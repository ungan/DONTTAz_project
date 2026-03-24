 LOG  Fonts loaded successfully
 LOG  [AuthDebug][SplashPage] {"authStatus": "loading"}
 LOG  [AuthDebug][useAppEntryCheck] {"baseUrl": "http://172.20.10.8:8080/api/v1", "hasAccessToken": true, "hasDevicePin": false, "hasRefreshToken": true, "isMock": "false", "nextStatus": "authenticated"}
 LOG  [AuthDebug][SplashPage] {"authStatus": "authenticated"}
 LOG  [AuthDebug][SplashPage] redirect -> /(tabs)/home
 LOG  [AuthDebug][useAuthCheck] {"hasAccessToken": true, "isMock": "false", "nextStatus": "authenticated"}
 LOG  [AuthDebug][useAppEntryCheck] {"baseUrl": "http://172.20.10.8:8080/api/v1", "hasAccessToken": true, "hasDevicePin": false, "hasRefreshToken": true, "isMock": "false", "nextStatus": "authenticated"}
 LOG  [BurndownDebug][Hook] effect:start {"reloadKey": 0, "source": "home-dday-slide", "yearMonth": "2026-03"}
 LOG  [BurndownDebug][Hook] fetch:start {"source": "home-dday-slide", "yearMonth": "2026-03"}
 LOG  [BurndownDebug][API] request {"accessTokenPreview": "eyJhbGciOiJI...", "baseUrl": "http://172.20.10.8:8080/api/v1", "hasAccessToken": true, "requestId": 1, "source": "home-dday-slide", "url": "/control/burndown", "yearMonth": "2026-03"}
 LOG  [BurndownDebug][API] success {"chartDataLength": 31, "message": "생존 번다운 차트 데이터 조회 성공", "requestId": 1, "source": "home-dday-slide", "status": "SUCCESS", "targetYearMonth": "2026-03"}
 LOG  [BurndownDebug][Hook] fetch:success {"chartDataLength": 31, "hasData": true, "source": "home-dday-slide"}
 LOG  [BurndownDebug][Hook] fetch:end {"source": "home-dday-slide", "yearMonth": "2026-03"}
 LOG  [BurndownDebug][PlanPage] render {"chartDataLength": 0, "error": null, "hasData": false, "hasFetched": false, "isListView": false, "isPending": true, "noPlan": false, "showEmpty": false, "showError": false, "showLoading": true, "source": "plan-page", "yearMonth": "2026-03"}  
 LOG  [BurndownDebug][Hook] effect:start {"reloadKey": 0, "source": "plan-page", "yearMonth": "2026-03"}
 LOG  [BurndownDebug][Hook] fetch:start {"source": "plan-page", "yearMonth": "2026-03"}
 LOG  [BurndownDebug][API] request {"accessTokenPreview": "eyJhbGciOiJI...", "baseUrl": "http://172.20.10.8:8080/api/v1", "hasAccessToken": true, "requestId": 2, "source": "plan-page", "url": "/control/burndown", "yearMonth": "2026-03"}
 LOG  [BurndownDebug][API] success {"chartDataLength": 31, "message": "생존 번다운 차트 데이터 조회 성공", "requestId": 2, "source": "plan-page", "status": "SUCCESS", "targetYearMonth": "2026-03"}
 LOG  [BurndownDebug][Hook] fetch:success {"chartDataLength": 31, "hasData": true, "source": "plan-page"}
 LOG  [BurndownDebug][Hook] fetch:end {"source": "plan-page", "yearMonth": "2026-03"}
 LOG  [BurndownDebug][PlanPage] render {"chartDataLength": 31, "error": null, "hasData": true, "hasFetched": true, "isListView": false, "isPending": false, "noPlan": false, "showEmpty": false, "showError": false, "showLoading": false, "source": "plan-page", "yearMonth": "2026-03"} 

2026-03-24T10:03:54.935+09:00  INFO 30460 --- [Donttaz] [nio-8080-exec-7] c.d.d.ai.service.AiTransactionService    : No spending plan found for userId=32 yearMonth=2026-03. Returning local dummy burndown data.
Hibernate: 
    select
        t1_0.account_tx_id,
        t1_0.account_id,
        t1_0.ai_category,
        t1_0.amount,
        t1_0.balance_snapshot,
        t1_0.counterparty_account_no,
        t1_0.counterparty_bank,
        t1_0.counterparty_name,
        t1_0.created_at,
        t1_0.is_deleted,
        t1_0.source,
        t1_0.source_tx_id,
        t1_0.synced_at,
        t1_0.transaction_at,
        t1_0.transaction_summary,
        t1_0.tx_type,
        t1_0.tx_type_name,
        t1_0.updated_at,
        t1_0.user_id 
    from
        account_transaction t1_0 
    left join
        transaction_ai_metadata tam1_0 
            on tam1_0.account_tx_id=t1_0.account_tx_id 
    where
        t1_0.user_id=? 
        and t1_0.transaction_at>=? 
        and t1_0.transaction_at<? 
    order by
        t1_0.transaction_at desc
2026-03-24T10:03:55.277+09:00  INFO 30460 --- [Donttaz] [nio-8080-exec-7] c.d.d.common.logging.ApiLoggingAspect    : [API LOG] IP: 172.20.10.1 | GET /api/v1/control/burndown | Time: 653ms
