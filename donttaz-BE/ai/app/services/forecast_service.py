import pickle
from pathlib import Path
import numpy as np
import pandas as pd

_model_path = Path(__file__).resolve().parent.parent / "models" / "patchtst_global.pkl"
with open(_model_path, "rb") as f:
    _global_model = pickle.load(f)


class ForecastService:
    def __init__(self):
        self.nf = _global_model

    def build_daily_series(self, transactions: list[dict], user_id: str = "user_1", payday: int = 25) -> pd.DataFrame:
        rows = []
        for tx in transactions:
            if tx.get("type") == "DEPOSIT":
                continue

            amount = int(tx.get("amount", 0))
            if amount <= 0:
                continue

            date_str = str(tx.get("date", ""))[:10]
            if not date_str:
                continue

            rows.append({
                "unique_id": user_id,
                "ds": date_str,
                "y": amount,
            })

        if not rows:
            return pd.DataFrame(columns=["unique_id", "ds", "y", "weekday", "is_weekend", "salary_relative_day"])

        df = pd.DataFrame(rows)
        df["ds"] = pd.to_datetime(df["ds"])
        df = df.groupby(["unique_id", "ds"], as_index=False)["y"].sum().sort_values("ds")

        # 학습 시 썼던 futr_exog 보정
        df["weekday"] = df["ds"].dt.weekday
        df["is_weekend"] = (df["weekday"] >= 5).astype(int)
        df["salary_relative_day"] = df["ds"].dt.day - payday

        return df

    def get_weekday_avg(self, transactions: list[dict], user_id: str = "user_1") -> dict:
        """요일(0=월~6=일)별 평균 지출 반환. 7일 이후 예측 확장에 사용."""
        df = self.build_daily_series(transactions, user_id=user_id)
        if df.empty:
            return {}
        return df.groupby(df["ds"].dt.weekday)["y"].mean().to_dict()

    def calculate_historical_mae_ratio(self, transactions: list[dict], user_id: str = "user_1") -> float:
        """
        과거 데이터 마지막 7일을 ground truth로 rolling mean MAE 비율을 계산.
        alpha 하한선으로 사용한다. 데이터 부족 시 0.15 반환.
        """
        df = self.build_daily_series(transactions, user_id=user_id)
        if len(df) < 14:
            return 0.15
        train = df.iloc[:-7]
        actual = df.iloc[-7:]["y"].values
        rolling_mean = train["y"].tail(14).mean()
        predicted = np.full(7, rolling_mean)
        mae = float(np.mean(np.abs(actual - predicted)))
        mean_actual = float(np.mean(actual))
        return float(mae / mean_actual) if mean_actual > 0 else 0.15

    def get_recent_daily_amounts(self, transactions: list[dict], user_id: str = "user_1", payday: int = 25) -> list[int]:
        df = self.build_daily_series(transactions, user_id=user_id, payday=payday)
        if df.empty:
            return []
        return [int(v) for v in df["y"].tolist()]

    def predict_next_7_days(self, transactions: list[dict], user_id: str = "user_1", payday: int = 25) -> list[dict]:
        df = self.build_daily_series(transactions, user_id=user_id, payday=payday)

        if df.empty:
            return []

        try:
            fcst = self.nf.predict(df=df).reset_index()
        except Exception:
            return []

        pred_col = None
        for c in fcst.columns:
            if "PatchTST" in c:
                pred_col = c
                break

        if pred_col is None:
            raise ValueError(f"PatchTST 예측 컬럼을 찾지 못했습니다. 현재 컬럼: {list(fcst.columns)}")

        results = []
        for _, row in fcst.iterrows():
            predicted = max(float(row[pred_col]), 0.0)
            predicted = round(predicted)

            if predicted < 1000:
                predicted = 0

            results.append({
                "date": str(pd.to_datetime(row["ds"]).date()),
                "predictedSpending": predicted,
            })

        return results