import { TokenRefreshResponse } from "@/auth/types/token-refresh-response";
import { Tokens } from "@/auth/types/tokens";
import { User } from "@/auth/types/user";
import serverEnv from "@/core/libs/env/server-env";
import axios from "axios";
import NextAuth from "next-auth";
import Auth0, { Auth0Profile } from "next-auth/providers/auth0";

const calculateTokenExpirationTime = (expiresIn: number) =>
  Math.floor(Date.now() / 1000 + expiresIn);

const hasTokenExpired = (expiresAt: number) => Date.now() < expiresAt * 1000;

export const { handlers, signIn, signOut, auth } = NextAuth({
  providers: [
    Auth0({
      authorization: {
        params: { scope: "openid profile email offline_access" },
      },
    }),
  ],
  pages: {
    signIn: "/sign-in",
  },
  callbacks: {
    authorized: async ({ auth }) => {
      // Logged in users are authenticated, otherwise redirect to login page
      return !!auth;
    },
    jwt: async ({ token, account, profile }) => {
      const auth0Profile = profile as Auth0Profile | undefined;

      // First-time login
      if (account && auth0Profile) {
        const user: User = {
          id: auth0Profile.sub,
          username: auth0Profile.preferred_username,
          email: auth0Profile.email,
          picture: auth0Profile.picture,
          roles: profile?.realm_access?.roles,
        };

        const tokens: Tokens = {
          accessToken: account.access_token,
          accessTokenExpiresIn: account.expires_in,
          accessTokenExpiresAt: account.expires_at,
          refreshToken: account.refresh_token,
          idToken: account.id_token,
        };

        return { user, tokens };
      }
      // Subsequent logins, but the `access_token` is still valid
      else if (
        token.tokens?.accessTokenExpiresAt &&
        hasTokenExpired(token.tokens.accessTokenExpiresAt)
      ) {
        return token;
      }
      // Subsequent logins, but the `access_token` has expired, try to refresh it
      else {
        try {
          const { data: newTokens } = await axios.post<TokenRefreshResponse>(
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

          return {
            ...token,
            tokens: {
              ...token.tokens,
              accessToken: newTokens.access_token,
              accessTokenExpiresIn: newTokens.expires_in,
              accessTokenExpiresAt: calculateTokenExpirationTime(
                newTokens.expires_in,
              ),
              idToken: newTokens.id_token,
            },
          };
        } catch {
          // Refresh token expired
          return null;
        }
      }
    },
    session: ({ session, token }) => {
      return { ...session, ...token };
    },
  },
});
