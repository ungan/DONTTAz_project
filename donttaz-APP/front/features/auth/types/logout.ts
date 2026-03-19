export interface LogoutRequest {
  refreshToken: string;
}

export interface LogoutResponse {
  status: string;
  message: string;
  data: null;
}
