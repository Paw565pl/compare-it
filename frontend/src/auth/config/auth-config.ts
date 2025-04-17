import { createTokens } from "@/auth/utils/create-tokens";
import { createUser } from "@/auth/utils/create-user";
import { refreshToken } from "@/auth/utils/refresh-token";
import NextAuth from "next-auth";
import Auth0, { Auth0Profile } from "next-auth/providers/auth0";

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
      return !!auth;
    },
    jwt: async ({ token, account, profile, trigger }) => {
      // manual server side or client side refresh was requested
      if (trigger === "update" || trigger === undefined)
        return refreshToken(token);

      // first-time login
      if (account && profile) {
        const auth0Profile = profile as Auth0Profile;

        const user = createUser(auth0Profile);
        const tokens = createTokens(account);

        return { user, tokens };
      }
      // subsequent logins, but the `access_token` is still valid
      else if (
        token.tokens?.accessTokenExpiresAt &&
        hasTokenExpired(token.tokens.accessTokenExpiresAt)
      ) {
        return token;
      }
      // subsequent logins, but the `access_token` has expired, try to refresh it
      else {
        return refreshToken(token);
      }
    },
    session: ({ session, token }) => {
      return { ...session, ...token };
    },
  },
});
