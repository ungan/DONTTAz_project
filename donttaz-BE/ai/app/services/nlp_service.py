from __future__ import annotations

from typing import List, Optional

import numpy as np

try:
    from sentence_transformers import SentenceTransformer
except Exception:
    SentenceTransformer = None


class NLPService:
    def __init__(self):
        # 1) 룰 기반 사전
        self.category_rules = {
            "식비": [
                "배달의민족", "요기요", "쿠팡이츠", "맥도날드", "버거킹","우아한 형제들",
                "롯데리아", "서브웨이", "김밥천국", "한식", "중식", "치킨", "피자", "식당", "스타벅스", "이디야",
            ],
            "쇼핑": [
                "쿠팡", "11번가", "지그재그", "에이블리", "무신사",
                "오늘의집", "네이버쇼핑", "g마켓", "옥션", "백화점","애플스토어"
            ],
            "교통": [
                "카카오t", "택시", "지하철", "버스", "srt", "ktx",
                "주유", "sk에너지", "gs칼텍스", "현대오일뱅크", "주차장"
            ],
            "생활": [
                "다이소", "올리브영", "이마트", "홈플러스", "마트",
                "gs25", "cu", "세븐일레븐", "편의점", "세탁", "생활"
            ],
            "여가": [
                "넷플릭스", "유튜브", "cgv", "메가박스", "롯데시네마",
                "스팀", "게임", "영화", "여행", "도서",
            ],
            "의료/건강": [
                "병원", "약국", "의원", "치과", "한의원", "헬스", "필라테스", "운동"
            ],
            "교육": [
                "학원", "인강", "응시료", "토익", "수강료", "교육", "스터디","교보문고","영풍문고","yes24"
            ],
            "금융/보험": [
                "보험", "카드대금", "이자", "원금상환", "자동이체", "정기결제", "통신비", "관리비"
            ],
            "경조사/선물": [
                "축의금", "조의금", "기프티콘", "선물", "꽃집"
            ],
        }

        # 2) SBERT fallback용 대표 merchant
        self.prototype_merchants = {
            "식비": ["배달의민족", "맥도날드", "식당", "치킨", "피자"],
            "쇼핑": ["쿠팡", "11번가", "무신사", "에이블리", "백화점"],
            "교통": ["카카오T", "지하철", "버스", "SK에너지", "주유소"],
            "생활": ["다이소", "올리브영", "이마트", "편의점", "홈플러스"],
            "여가": ["넷플릭스", "CGV", "스팀", "영화관", "교보문고"],
            "의료/건강": ["병원", "약국", "헬스장", "필라테스", "운동"],
            "교육": ["학원", "인강", "응시료", "수강료", "교육"],
            "금융/보험": ["보험", "카드대금", "이자", "관리비", "통신비"],
            "경조사/선물": ["축의금", "조의금", "기프티콘", "선물"],
        }   

        self.use_embedding = False
        self.model = None
        self.prototype_embeddings = None
        self.prototype_labels: List[str] = []
        self.prototype_texts: List[str] = []

        self._init_embedding_model()

    def _init_embedding_model(self):
        """
        모델 로딩 실패 시 룰 기반만 사용하도록 안전하게 처리
        """
        if SentenceTransformer is None:
            return

        try:
            # 오늘 바로 붙이기엔 이 모델이 가장 무난
            self.model = SentenceTransformer("jhgan/ko-sbert-multitask")

            texts = []
            labels = []
            for category, merchants in self.prototype_merchants.items():
                for merchant in merchants:
                    texts.append(self._normalize(merchant))
                    labels.append(category)

            embeddings = self.model.encode(texts, normalize_embeddings=True)

            self.prototype_texts = texts
            self.prototype_labels = labels
            self.prototype_embeddings = embeddings
            self.use_embedding = True
        except Exception:
            self.use_embedding = False
            self.model = None
            self.prototype_embeddings = None
            self.prototype_labels = []
            self.prototype_texts = []

    def _normalize(self, text: str) -> str:
        if not text:
            return ""
        return (
            str(text)
            .strip()
            .lower()
            .replace(" ", "")
            .replace("_", "")
            .replace("-", "")
        )

    def _rule_based_match(self, merchant: str) -> Optional[dict]:
        merchant_norm = self._normalize(merchant)

        # 완전 일치 > 부분 일치 순
        for category, keywords in self.category_rules.items():
            for keyword in keywords:
                keyword_norm = self._normalize(keyword)

                if merchant_norm == keyword_norm:
                    return {
                        "category": category,
                        "confidenceScore": 0.98,
                        "matchedKeyword": keyword,
                        "matchType": "rule_exact",
                    }

        for category, keywords in self.category_rules.items():
            for keyword in keywords:
                keyword_norm = self._normalize(keyword)

                if keyword_norm and keyword_norm in merchant_norm:
                    return {
                        "category": category,
                        "confidenceScore": 0.90,
                        "matchedKeyword": keyword,
                        "matchType": "rule_partial",
                    }

        return None

    def _embedding_match(self, merchant: str) -> dict:
        if not self.use_embedding or self.model is None:
            return {
                "category": "기타",
                "confidenceScore": 0.30,
                "matchedKeyword": None,
                "matchType": "default",
            }

        merchant_norm = self._normalize(merchant)
        emb = self.model.encode([merchant_norm], normalize_embeddings=True)[0]

        scores = np.dot(self.prototype_embeddings, emb)
        best_idx = int(np.argmax(scores))
        best_score = float(scores[best_idx])
        best_category = self.prototype_labels[best_idx]
        best_text = self.prototype_texts[best_idx]

        # 점수 보정
        # cosine 유사도라 보통 0.3~0.9 사이
        if best_score >= 0.65:
            confidence = round(best_score, 3)
        elif best_score >= 0.50:
            confidence = 0.50
        else:
            # 너무 애매하면 기타로 낮은 신뢰도 fallback
            return {
                "category": "기타",
                "confidenceScore": 0.35,
                "matchedKeyword": None,
                "matchType": "default",
            }

        return {
            "category": best_category,
            "confidenceScore": confidence,
            "matchedKeyword": best_text,
            "matchType": "embedding",
        }

    def categorize_transaction(self, merchant: str) -> dict:
        # 1차: 룰 기반
        rule_result = self._rule_based_match(merchant)
        if rule_result is not None:
            return rule_result

        # 2차: SBERT fallback
        return self._embedding_match(merchant)

    def categorize_transactions(self, transactions: List[dict]) -> List[dict]:
        results = []

        for tx in transactions:
            new_tx = tx.copy()

            # 1. 수입은 그대로 유지
            if tx.get("type") == "DEPOSIT":
                new_tx["category"] = "수입"
                new_tx["confidenceScore"] = 1.0
                new_tx["matchedKeyword"] = None
                new_tx["matchType"] = "deposit"
                new_tx["isModified"] = False
                results.append(new_tx)
                continue

            # 2. SSAFY 등 외부에서 이미 category가 있으면 우선 사용
            existing_category = tx.get("category")
            if existing_category and str(existing_category).strip() not in ["", "기타", "unknown", "UNKNOWN", None]:
                new_tx["category"] = existing_category
                new_tx["confidenceScore"] = 1.0
                new_tx["matchedKeyword"] = None
                new_tx["matchType"] = "ssafy_category"
                new_tx["isModified"] = False
                results.append(new_tx)
                continue

            # 3. category가 없거나 기타이면 merchant 기반 분류
            merchant_text = tx.get("merchant") or tx.get("storeName") or ""
            pred = self.categorize_transaction(merchant_text)

            new_tx["category"] = pred["category"]
            new_tx["confidenceScore"] = pred["confidenceScore"]
            new_tx["matchedKeyword"] = pred["matchedKeyword"]
            new_tx["matchType"] = pred["matchType"]
            new_tx["isModified"] = False

            results.append(new_tx)

        return results


# 모듈 로딩 시 1회만 초기화 (SBERT 모델 재로딩 방지)
_nlp_instance = NLPService()