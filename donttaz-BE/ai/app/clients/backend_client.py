class BackendClientDisabledError(RuntimeError):
    pass


def get_transactions(*args, **kwargs):
    raise BackendClientDisabledError(
        "AI 서버의 직접 백엔드 호출은 비활성화되었습니다. "
        "거래 데이터는 백엔드가 수집해 AI 서버 요청 본문으로 전달해야 합니다."
    )
