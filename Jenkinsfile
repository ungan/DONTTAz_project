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
        stage('Prepare Backend') {
            steps {
                dir('donttaz-BE') { 
                    script {
                        echo "🚀 [Donttaz] Backend 변경 감지! 보안 환경 설정 시작..."

                        // [선택] FCM 푸시 알림용 Firebase 키 파일 주입
                        // withCredentials([file(credentialsId: 'FIREBASE_KEY_FILE', variable: 'FIREBASE_KEY_PATH')]) {
                        //     // Spring Boot의 resources 폴더로 복사 (경로는 실제에 맞게 수정 필요)
                        //     sh "cp \$FIREBASE_KEY_PATH ./api/src/main/resources/firebase-service-account.json"
                        //     sh "chmod 644 ./api/src/main/resources/firebase-service-account.json"
                        //     echo "🔑 Firebase Key file injected successfully."
                        // }

                        // Jenkins Credential에서 환경 변수 가져와서 .env 파일 생성
                        withCredentials([
                            string(credentialsId: 'FIN_API_BASE_URL', variable: 'FIN_API_BASE_URL'),
                            string(credentialsId: 'FIN_API_API_KEY', variable: 'FIN_API_API_KEY'),
                            string(credentialsId: 'FIN_API_MOCK_USER_KEY', variable: 'FIN_API_MOCK_USER_KEY'),
                            string(credentialsId: 'DB_URL', variable: 'DB_URL'),
                            string(credentialsId: 'DB_USERNAME', variable: 'DB_USERNAME'),
                            string(credentialsId: 'DB_PASSWORD', variable: 'DB_PASSWORD'),
                            string(credentialsId: 'REDIS_HOST', variable: 'REDIS_HOST'),
                            string(credentialsId: 'REDIS_PORT', variable: 'REDIS_PORT')
                        ]) {
                            sh """
                            echo "FIN_API_BASE_URL=${FIN_API_BASE_URL}" > .env
                            echo "FIN_API_API_KEY=${FIN_API_API_KEY}" >> .env
                            echo "FIN_API_MOCK_USER_KEY=${FIN_API_MOCK_USER_KEY}" >> .env
                            echo "DB_URL=${DB_URL}" >> .env
                            echo "DB_USERNAME=${DB_USERNAME}" >> .env
                            echo "DB_PASSWORD=${DB_PASSWORD}" >> .env
                            echo "REDIS_HOST=${REDIS_HOST}" >> .env
                            echo "REDIS_PORT=${REDIS_PORT}" >> .env
                            """
                            echo "🔐 .env file created securely."
                        }
                    }
                }
            }
        }

        // =========================================================
        // 2. 서비스 무중단 배포 (Blue-Green Deploy)
        // =========================================================
        stage('Zero-Downtime Deploy') {
            when {
                anyOf {
                    changeset "donttaz-BE/api/**"
                    changeset "donttaz-BE/ai/**"
                }
            }
            steps {
                dir('donttaz-BE') {
                    echo "🚀 백엔드 변경 감지! 무중단 배포 스크립트를 실행합니다..."
                    
                    // 실행 권한 부여
                    sh 'chmod +x scripts/deploy.sh'
                    
                    // 스크립트 실행
                    sh './scripts/deploy.sh'
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
            dir('donttaz-BE') { 
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