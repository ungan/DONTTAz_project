# Grafana 모니터링 가이드

이 가이드는 API 서버 모니터링을 위한 대시보드(19004) 추가 및 Blue-Green 배포 시각화 방법을 안내합니다.

## 1. API 서버 상세 모니터링 (Dashboard 19004)
Spring Boot API 서버의 상세 상태를 파악하기 위해 다음 단계를 수행하세요.

1. Grafana 접속 (`http://<서버IP>:3000`)
2. 왼쪽 메뉴에서 **Dashboards** > **New** > **Import** 클릭
3. **Import via grafana.com** 입력창에 `19004` 입력 후 **Load** 클릭
4. **Prometheus** 데이터 소스를 선택하고 **Import** 완료

## 2. Blue-Green 배포 시각화 구성
배포 시 Blue/Green 서버의 전환 과정을 시각화하려면 다음 쿼리를 활용하여 패널을 만드세요.

### A. 실시간 가동 상태 (Stat Panel)
- **Query**: `up{job="donttaz-api"}`
- **Legend**: `{{instance}}`
- **설명**: 현재 어떤 인스턴스(blue/green)가 `1`(Up) 상태인지 직관적으로 보여줍니다.

### B. 배포 전환 히스토리 (State Timeline)
- **Query**: `up{job="donttaz-api"}`
- **Visualization**: **State timeline** 선택
- **설명**: 시간에 따라 blue와 green 서버가 교차로 가동/중지되는 과정을 타임라인으로 확인할 수 있습니다.

---
**참고**: API 서버에 `micrometer-registry-prometheus` 의존성이 추가되어 있어야 하며, `/actuator/prometheus` 경로가 노출되어야 정상적으로 메트릭이 수집됩니다.
