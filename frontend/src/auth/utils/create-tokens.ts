import { Tokens } from "@/auth/types/tokens";
import { Account } from "next-auth";

export const createTokens = (account: Account): Tokens => ({
  accessToken: account.access_token,
  accessTokenExpiresIn: account.expires_in,
  accessTokenExpiresAt: account.expires_at,
  refreshToken: account.refresh_token,
  idToken: account.id_token,
});
