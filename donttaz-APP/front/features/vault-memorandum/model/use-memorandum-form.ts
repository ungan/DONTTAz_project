import type { MemorandumRequest } from '@/entities/vault';

const CONTENT1 =
  '위시리스트 목표를 달성하기 전 긴급 출금을 시도할 경우, 본인이 사전에 지정한 지인들에게 수치심 메시지가 자동으로 발송됨을 인지하고 이에 동의한다.';

const CONTENT2 =
  '위시리스트 금고를 목표 달성 전 중도 해지할 경우, 지정된 지인들에게 동일하게 수치심 메시지가 자동 발송됨을 인지하고 이에 동의한다.';

const CONTENT3 =
  '본 각서는 위시리스트 목표 금액 달성 시까지 유효하며, 목표 달성과 동시에 자동으로 효력을 잃는다.';

export function useMemorandumForm() {
  const getRequestBody = (): MemorandumRequest => ({
    content1: CONTENT1,
    content2: CONTENT2,
    content3: CONTENT3,
  });

  return { getRequestBody };
}
