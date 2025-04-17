import { TokenRefreshResponse } from "@/auth/types/token-refresh-response";
import { User } from "@/auth/types/user";
import { createUser } from "@/auth/utils/create-user";
import serverEnv from "@/core/libs/env/server-env";
import axios, { isAxiosError } from "axios";
import { decodeJwt } from "jose";
import { JWT } from "next-auth/jwt";
import { Auth0Profile } from "next-auth/providers/auth0";

const calculateTokenExpirationTime = (expiresIn: number) =>
  Math.floor(Date.now() / 1000 + expiresIn);

export const refreshTokens = async (token: JWT): Promise<JWT | null> => {
  console.log("PERFORMING TOKEN REFRESH ...");

  try {
    const {
      data: { access_token, refresh_token, id_token, expires_in },
    } = await axios.post<TokenRefreshResponse>(
      serverEnv.AUTH_AUTH0_ISSUER + "/oauth/token",
      {
        grant_type: "refresh_token",
        refresh_token: token.tokens?.refreshToken,
        client_id: serverEnv.AUTH_AUTH0_ID,
        client_secret: serverEnv.AUTH_AUTH0_SECRET,
      },
      {
        headers: {
          "Content-Type": "application/x-www-form-urlencoded",
        },
      },
    );

    const auth0Profile = id_token ? decodeJwt<Auth0Profile>(id_token) : null;
    const user: User | undefined = auth0Profile
      ? createUser(auth0Profile)
      : token.user; // fall back to old properties

    return {
      user,
      tokens: {
        accessToken: access_token,
        accessTokenExpiresIn: expires_in,
        accessTokenExpiresAt: calculateTokenExpirationTime(expires_in),
        // fall back to old refresh token
        refreshToken: refresh_token ?? token.tokens?.refreshToken,
        idToken: id_token,
      },
    };
  } catch (e) {
    if (isAxiosError(e)) console.log(e.response?.data, e.response?.status);
    else console.log(e);

    // Refresh token expired
    return null;
  }
};
