import re
from typing import Dict

class CategoryNLPService:

    def __init__(self):
        self.category_rules = {
            "식비": [
                "스타벅스", "이디야", "투썸", "커피",
                "맥도날드", "버거킹", "롯데리아",
                "치킨", "피자", "배달", "식당", "카페"
            ],
            "편의점": [
                "CU", "GS25", "세븐일레븐", "이마트24"
            ],
            "쇼핑": [
                "쿠팡", "G마켓", "11번가", "옥션",
                "올리브영", "무신사", "지그재그"
            ],
            "교통": [
                "택시", "카카오택시", "우버",
                "지하철", "버스"
            ],
            "주유": [
                "주유소", "GS칼텍스", "SK에너지", "S-OIL"
            ],
            "구독": [
                "넷플릭스", "유튜브", "멜론", "스포티파이"
            ],
        }

    def classify(self, merchant_name: str) -> Dict:
        """
        가맹점명을 기반으로 카테고리 분류   ---> 키워드 매칭
        """

        merchant_name = merchant_name.lower()

        for category, keywords in self.category_rules.items():
            for keyword in keywords:
                if keyword.lower() in merchant_name:
                    return {
                        "category": category,
                        "confidence": 0.9
                    }

        return {
            "category": "기타",
            "confidence": 0.5
        }