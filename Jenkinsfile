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
                            string(credentialsId: 'DB_URL', variable: 'DB_URL'),
                            string(credentialsId: 'DB_USERNAME', variable: 'DB_USERNAME'),
                            string(credentialsId: 'DB_PASSWORD', variable: 'DB_PASSWORD'),
                            string(credentialsId: 'REDIS_HOST', variable: 'REDIS_HOST'),
                            string(credentialsId: 'REDIS_PORT', variable: 'REDIS_PORT'),
                            string(credentialsId: 'RABBITMQ_HOST', variable: 'RABBITMQ_HOST'),
                            string(credentialsId: 'RABBITMQ_PORT', variable: 'RABBITMQ_PORT'),
                            string(credentialsId: 'RABBITMQ_USERNAME', variable: 'RABBITMQ_USERNAME'),
                            string(credentialsId: 'RABBITMQ_PASSWORD', variable: 'RABBITMQ_PASSWORD'),
                            string(credentialsId: 'KAKAO_CLIENT_ID', variable: 'KAKAO_CLIENT_ID'),
                            string(credentialsId: 'KAKAO_CLIENT_SECRET', variable: 'KAKAO_CLIENT_SECRET'),
                            string(credentialsId: 'API_SECRET', variable: 'API_SECRET')
                        ]) {
                            sh """
                            echo "FIN_API_BASE_URL=${FIN_API_BASE_URL}" > .env
                            echo "FIN_API_API_KEY=${FIN_API_API_KEY}" >> .env
                            echo "DB_URL=${DB_URL}" >> .env
                            echo "DB_USERNAME=${DB_USERNAME}" >> .env
                            echo "DB_PASSWORD=${DB_PASSWORD}" >> .env
                            echo "REDIS_HOST=${REDIS_HOST}" >> .env
                            echo "REDIS_PORT=${REDIS_PORT}" >> .env
                            echo "RABBITMQ_HOST=${RABBITMQ_HOST}" >> .env
                            echo "RABBITMQ_PORT=${RABBITMQ_PORT}" >> .env
                            echo "RABBITMQ_USERNAME=${RABBITMQ_USERNAME}" >> .env
                            echo "RABBITMQ_PASSWORD=${RABBITMQ_PASSWORD}" >> .env
                            echo "KAKAO_CLIENT_ID=${KAKAO_CLIENT_ID}" >> .env
                            echo "KAKAO_CLIENT_SECRET=${KAKAO_CLIENT_SECRET}" >> .env
                            echo "API_SECRET=${API_SECRET}" >> .env
                            """
                            echo "🔐 .env file created securely."
                        }
                    }
                }
            }
        }

        // =========================================================
        // 1.5. 단위 테스트 (Continuous Testing)
        // =========================================================
        stage('Test Backend (CT)') {
            when {
                changeset "donttaz-BE/api/**"
            }
            steps {
                dir('donttaz-BE/api') {
                    echo "🧪 Spring Boot 단위 테스트 실행 중..."
                    script {
                        sh 'chmod +x gradlew'
                        // 결과값을 파일로 빼고 상태 코드를 잡습니다. (Mattermost 알림용)
                        def status = sh(script: './gradlew clean test > test_output.log 2>&1', returnStatus: true)
                        if (status != 0) {
                            env.FAIL_STAGE = "Test (Unit Test)"
                            env.FAIL_LOG = sh(script: 'tail -n 25 test_output.log', returnStdout: true).trim()
                            error("🚨 단위 테스트 실패!")
                        } else {
                            // 성공 시 콘솔에서 볼 수 있게 로그를 출력시켜줍니다.
                            sh 'cat test_output.log'
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
                    changeset "donttaz-BE/nginx/**"
                    changeset "donttaz-BE/scripts/**"
                    changeset "Jenkinsfile"
                }
            }
            steps {
                dir('donttaz-BE') {
                    echo "🚀 배포 시작! Nginx 상태 점검 및 무중단 배포를 진행합니다..."
                    
                    // 실행 권한 부여
                    sh 'chmod +x scripts/deploy.sh'
                    
                    // 스크립트 실행 및 에러 캐치 구조화
                    script {
                        def status = sh(script: 'set -o pipefail; bash -x ./scripts/deploy.sh | tee deploy_output.log', returnStatus: true)
                        if (status != 0) {
                            env.FAIL_STAGE = "Deploy (Blue-Green)"
                            env.FAIL_LOG = sh(script: 'tail -n 25 deploy_output.log', returnStdout: true).trim()
                            error("🚨 배포 스크립트 실행 실패!")
                        } else {
                            sh 'cat deploy_output.log'
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
            script {
                def webhookUrl = 'https://meeting.ssafy.com/hooks/fqz6a9tj63gdub7tcaydjmhnho'
                def text = "## :jenkins7: Backend Deploy 성공 ✅\\n**Jenkins**: [빌드 보기](${env.BUILD_URL})"
                def payloadMap = [
                    text: text,
                    username: "Jenkins",
                    icon_emoji: ":jenkins7:"
                ]
                // JSON 문법 충돌을 방지하기 위해 파일 쓰기 우회 기법 사용
                writeFile file: 'success_payload.json', text: groovy.json.JsonOutput.toJson(payloadMap)
                sh "curl -sS -H 'Content-Type: application/json' --data-binary @success_payload.json ${webhookUrl} || true"
            }
        }
        failure {
            echo "💥 [Donttaz] 배포 중 오류가 발생했습니다. 로그를 확인해 주세요."
            script {
                def webhookUrl = 'https://meeting.ssafy.com/hooks/fqz6a9tj63gdub7tcaydjmhnho'
                def failStage = env.FAIL_STAGE ?: "Unknown Stage"
                def failLog = env.FAIL_LOG ?: "로그를 가져올 수 없거나 파이프라인 내부 에러입니다."
                
                def text = "## :angry_jenkins: Backend 파이프라인 실패 ❌\\n**실패 구간:** ${failStage}\\n**에러 내용 (최근 25줄):**\\n```text\\n${failLog}\\n```\\n**Jenkins**: [해당 빌드 보기](${env.BUILD_URL})"
                
                def payloadMap = [
                    text: text,
                    username: "Jenkins",
                    icon_emoji: ":jenkins7:"
                ]
                writeFile file: 'fail_payload.json', text: groovy.json.JsonOutput.toJson(payloadMap)
                sh "curl -sS -H 'Content-Type: application/json' --data-binary @fail_payload.json ${webhookUrl} || true"
            }
        }
    }
}