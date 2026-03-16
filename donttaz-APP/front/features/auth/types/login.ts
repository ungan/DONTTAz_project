export interface LoginRequest {
  loginId: string;
  password: string;
}

export interface LoginResponseData {
  accessToken: string;
  refreshToken: string;
  userId: number;
  name: string;
}

export interface LoginResponse {
  status: string;
  message: string;
  data: LoginResponseData;
}
