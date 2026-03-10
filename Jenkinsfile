pipeline {
    agent any

    stages {
        stage('Checkout') {
            steps {
                checkout scm
            }
        }

        // =========================================================
        // 1. 공통 환경 설정 (백엔드 폴더 전체 중 변경이 있을 때만 실행)
        // =========================================================
        // stage('Prepare Backend') {
        //     when {
        //         changeset "donttaz-be/**"
        //     }
        //     steps {
        //         dir('donttaz-be') { 
        //             script {
        //                 echo "🚀 [Donttaz] Backend 변경 감지! 보안 환경 설정 시작..."

        //                 // [선택] FCM 푸시 알림용 Firebase 키 파일 주입
        //                 withCredentials([file(credentialsId: 'FIREBASE_KEY_FILE', variable: 'FIREBASE_KEY_PATH')]) {
        //                     // Spring Boot의 resources 폴더로 복사 (경로는 실제에 맞게 수정 필요)
        //                     sh "cp \$FIREBASE_KEY_PATH ./api/src/main/resources/firebase-service-account.json"
        //                     sh "chmod 644 ./api/src/main/resources/firebase-service-account.json"
        //                     echo "🔑 Firebase Key file injected successfully."
        //                 }

        //                 // Jenkins Credential에서 환경 변수 가져와서 .env 파일 생성
        //                 withCredentials([
        //                     // 환경 변수 추가 예정
        //                 ]) {
        //                     sh """
        //                     """
        //                     echo "🔐 .env file created securely."
        //                 }
        //             }
        //         }
        //     }
        // }

        // =========================================================
        // 2. 서비스별 조건부 배포 (변경된 서비스만 병렬 빌드)
        // =========================================================
        stage('Deploy Services') {
            parallel {
                // 1) Spring Boot (메인 뱅킹/결제 로직)
                stage('Deploy API (Spring Boot)') {
                    when {
                        changeset "donttaz-be/api/**"
                    }
                    steps {
                        dir('donttaz-be') {
                            echo "☕ Spring Boot API 변경 감지! 빌드 및 배포 시작..."
                            // Spring Boot 컨테이너 이름(api)만 지정하여 해당 부분만 재빌드
                            sh 'docker-compose up -d --build api'
                        }
                    }
                }
                
                // 2) FastAPI (AI 소비 내역 분석)
                stage('Deploy AI (FastAPI)') {
                    when {
                        changeset "donttaz-be/ai/**"
                    }
                    steps {
                        dir('donttaz-be') {
                            echo "🤖 FastAPI AI 변경 감지! 빌드 및 배포 시작..."
                            // FastAPI 컨테이너 이름(ai)만 지정하여 해당 부분만 재빌드
                            sh 'docker-compose up -d --build ai'
                        }
                    }
                }
            }
        }
        
        // =========================================================
        // 3. 공통 작업 (찌꺼기 이미지 정리)
        // =========================================================
        stage('Clean up Image') {
             steps {
                script {
                    echo "🧹 사용하지 않는 댕글링(Dangling) 도커 이미지 삭제..."
                    sh 'docker image prune -f'
                }
             }
        }
    }

    // =========================================================
    // 4. 파이프라인 종료 후 처리 (보안 클린업)
    // =========================================================
    post {
        always {
            dir('donttaz-be') { 
                script {
                    // 빌드가 끝나면 서버에 평문으로 남은 .env 파일과 json 키를 즉각 삭제 (보안 조치)
                    sh "rm -f .env"
                    sh "rm -f ./api/src/main/resources/firebase-service-account.json"
                    echo "🔒 Backend security cleanup completed."
                }
            }
        }
        success {
            echo "🎉 [Donttaz] 모든 배포가 성공적으로 완료되었습니다!"
            // [선택] 디스코드/매터모스트 웹훅으로 성공 알림 전송 가능
        }
        failure {
            echo "💥 [Donttaz] 배포 중 오류가 발생했습니다. 로그를 확인해 주세요."
        }
    }
}