#!/usr/bin/env python3
"""
DART CORPCODE.xml 에서 스팩/기업인수목적 회사 목록을 추출하고,
각 회사의 공시 이력을 조회하여 합병 성공/청산 여부를 분류하는 스크립트.

- CORPCODE.xml 다운로드 (없으면 자동 다운로드)
- 스팩 회사별로 공시 목록 조회 후 결과를 분기별 캐시 파일에 저장
- 중단 후 재실행해도 이미 완료된 항목은 건너뜀
- 마지막에 전체 합산 파일 생성

사용법:
  DART_API_KEY=<key> python3 fetch_spac_merge_history.py [--output-dir ./data]
"""

import os
import sys
import time
import json
import argparse
import requests
import zipfile
import xml.etree.ElementTree as ET
from datetime import datetime
from collections import Counter

sys.stdout.reconfigure(line_buffering=True)

DART_BASE_URL = "https://opendart.fss.or.kr/api"
BASE_DIR = "/Users/neo/build/spac/scripts"


def download_corpcode(api_key: str, cache_path: str) -> list[dict]:
    """DART CORPCODE.xml 다운로드 후 스팩 목록 반환"""
    zip_path = cache_path + ".zip"

    if not os.path.exists(cache_path):
        print("CORPCODE.xml 다운로드 중...")
        resp = requests.get(f"{DART_BASE_URL}/corpCode.xml",
                            params={"crtfc_key": api_key}, timeout=30)
        resp.raise_for_status()
        with open(zip_path, "wb") as f:
            f.write(resp.content)
        with zipfile.ZipFile(zip_path, "r") as z:
            z.extractall(os.path.dirname(cache_path))
        os.rename(os.path.join(os.path.dirname(cache_path), "CORPCODE.xml"), cache_path)
        print(f"저장: {cache_path}")

    tree = ET.parse(cache_path)
    root = tree.getroot()
    spac_list = []
    for item in root.findall("list"):
        name = item.findtext("corp_name", "")
        corp_code = item.findtext("corp_code", "")
        stock_code = item.findtext("stock_code", "").strip()
        if "스팩" in name or "기업인수목적" in name:
            spac_list.append({
                "corp_code": corp_code,
                "name": name,
                "stock_code": stock_code,
            })
    return spac_list


def fetch_disclosures(api_key: str, corp_code: str) -> list[dict]:
    """한 회사의 전체 공시 목록 조회"""
    all_items = []
    page_no = 1
    while True:
        try:
            resp = requests.get(f"{DART_BASE_URL}/list.json", params={
                "crtfc_key": api_key,
                "corp_code": corp_code,
                "bgn_de": "20100101",
                "end_de": "20261231",
                "page_count": 100,
                "page_no": page_no,
            }, timeout=15)
            d = resp.json()
        except Exception as e:
            print(f"    [오류] {e}")
            break

        if d.get("status") not in ("000",):
            break

        items = d.get("list") or []
        all_items.extend(items)

        total_page = d.get("total_page", 1)
        if page_no >= total_page:
            break
        page_no += 1
        time.sleep(0.15)

    return all_items


def classify(items: list[dict]) -> dict:
    """
    공시 목록을 분석하여 합병 성공/청산/진행중 분류.

    합병 성공:
      - 회사합병결정(또는 합병결정) 공시 있음
      - 해산사유발생 없음
      - 합병승인(예비심사결과 승인) 공시 있음

    청산:
      - 해산사유발생 공시 있음

    진행중/미확인:
      - 그 외
    """
    report_names = [item.get("report_nm", "") for item in items]

    has_merge_decision = any(
        "회사합병결정" in nm or ("합병결정" in nm and "취소" not in nm)
        for nm in report_names
    )
    has_merge_approved = any(
        "상장예비심사결과 통지(승인)" in nm or "상장예비심사결과통지(승인)" in nm
        or "합병승인" in nm
        for nm in report_names
    )
    has_dissolution = any("해산사유" in nm for nm in report_names)
    has_merge_cancel = any(
        "합병취소" in nm or "합병중단" in nm or "합병결정 철회" in nm
        for nm in report_names
    )

    # 합병 완료일 추정: 마지막 공시 이후 더 이상 없음
    latest_date = max((item.get("rcept_dt", "") for item in items), default="")
    merge_date = ""
    for item in items:
        if "합병결정" in item.get("report_nm", "") or "증권신고서(합병)" in item.get("report_nm", ""):
            merge_date = max(merge_date, item.get("rcept_dt", ""))

    if has_dissolution:
        status = "청산"
    elif has_merge_approved and not has_dissolution:
        status = "합병성공"
    elif has_merge_decision and not has_dissolution and not has_merge_cancel:
        status = "합병성공(추정)"
    elif has_merge_cancel:
        status = "합병취소→청산" if has_dissolution else "합병취소"
    else:
        status = "기타"

    return {
        "status": status,
        "merge_date": merge_date,
        "latest_date": latest_date,
        "report_count": len(items),
    }


def save_result(result: dict, path: str):
    with open(path, "w", encoding="utf-8") as f:
        json.dump(result, f, ensure_ascii=False, indent=2)


def load_result(path: str) -> dict | None:
    try:
        with open(path, encoding="utf-8") as f:
            return json.load(f)
    except Exception:
        return None


def main():
    parser = argparse.ArgumentParser()
    parser.add_argument("--api-key")
    parser.add_argument("--output-dir", default=os.path.join(BASE_DIR, "dart_spac_cache"))
    parser.add_argument("--output", default=os.path.join(BASE_DIR, "spac_merge_history.tsv"))
    args = parser.parse_args()

    api_key = args.api_key or os.environ.get("DART_API_KEY", "")
    if not api_key:
        print("❌ DART_API_KEY 필요")
        sys.exit(1)

    os.makedirs(args.output_dir, exist_ok=True)
    corpcode_path = os.path.join(args.output_dir, "CORPCODE.xml")

    # 1. 스팩 목록 수집
    spac_list = download_corpcode(api_key, corpcode_path)
    # 주식코드 있는 것만 (상장 이력 있음)
    listed = [s for s in spac_list if s["stock_code"]]
    print(f"조회 대상: {len(listed)}개 스팩/기업인수목적회사\n")

    # 2. 각 스팩 공시 조회
    results = []
    for i, spac in enumerate(listed, 1):
        cache_file = os.path.join(args.output_dir, f"{spac['corp_code']}.json")
        cached = load_result(cache_file)

        if cached:
            print(f"[{i:3d}/{len(listed)}] {spac['name']} → {cached['status']} (캐시)")
            results.append({**spac, **cached})
            continue

        print(f"[{i:3d}/{len(listed)}] {spac['name']} 조회 중...", end=" ", flush=True)
        items = fetch_disclosures(api_key, spac["corp_code"])
        info = classify(items)
        save_result(info, cache_file)
        print(f"→ {info['status']} (공시 {info['report_count']}건)")
        results.append({**spac, **info})
        time.sleep(0.2)

    # 3. 전체 결과 저장
    print(f"\n=== 결과 요약 ===")
    status_counts = Counter(r["status"] for r in results)
    for status, count in status_counts.most_common():
        print(f"  {status}: {count}건")

    # TSV 저장
    with open(args.output, "w", encoding="utf-8") as f:
        f.write("종목코드\t회사명\tcorp_code\t분류\t합병관련일\t마지막공시일\t공시수\n")
        for r in sorted(results, key=lambda x: x.get("merge_date", "") or ""):
            f.write(
                f"{r['stock_code']}\t{r['name']}\t{r['corp_code']}\t"
                f"{r['status']}\t{r.get('merge_date','')}\t"
                f"{r.get('latest_date','')}\t{r.get('report_count',0)}\n"
            )
    print(f"\n✅ 저장: {args.output} ({len(results)}건)")

    # 합병 성공 케이스만 별도 출력
    success = [r for r in results if "합병성공" in r["status"]]
    print(f"\n합병 성공 ({len(success)}건):")
    for r in sorted(success, key=lambda x: x.get("merge_date", "")):
        print(f"  [{r.get('merge_date','?')[:4]}] {r['name']} ({r['stock_code']}) - {r['status']}")

    # 증권사별 통계
    def extract_broker(name: str) -> str:
        for kw in ["기업인수목적", "스팩"]:
            if kw in name:
                return name.split(kw)[0].rstrip("0123456789호제 ")
        return name

    print(f"\n증권사별 합병 성공 건수:")
    broker_counts = Counter(extract_broker(r["name"]) for r in success)
    for broker, count in broker_counts.most_common(15):
        print(f"  {broker}: {count}건")


if __name__ == "__main__":
    main()
