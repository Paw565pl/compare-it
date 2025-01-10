import { Tokens } from "@/auth/types/tokens";
import { User } from "@/auth/types/user";
import NextAuth from "next-auth";
import Auth0, { Auth0Profile } from "next-auth/providers/auth0";

export const { handlers, signIn, signOut, auth } = NextAuth({
  providers: [
    Auth0({
      authorization: {
        params: { scope: "openid profile email offline_access" },
      },
    }),
  ],
  callbacks: {
    jwt: ({ token, account, profile }) => {
      const auth0Profile = profile as Auth0Profile | undefined;

      if (account && auth0Profile) {
        console.log("account");
        console.log(account);

        const user: User = {
          id: auth0Profile?.sub || "",
          username: auth0Profile?.preferred_username,
          email: auth0Profile?.email,
          picture: auth0Profile?.picture,
          roles: profile?.realm_access?.roles,
        };

        const tokens: Tokens = {
          accessToken: account.access_token,
          accessTokenExpiresIn: account.expires_in,
          accessTokenExpiresAt: account.expires_at,
          refreshToken: account.refresh_token,
        };

        return { user, tokens };
      } else {
        return token;
      }
    },
    session: ({ session, token }) => {
      return { ...session, ...token };
    },
  },
});
