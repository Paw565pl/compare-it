import { Role } from "@/auth/types/role";
import { Tokens } from "@/auth/types/tokens";
import { User } from "@/auth/types/user";
import "next-auth";

declare module "next-auth" {
  /**
   * Returned by `auth`, `useSession`, `getSession` and received as a prop on the `SessionProvider` React Context
   */
  interface Session {
    user?: User;
    tokens?: Tokens;
  }

  interface Profile {
    realm_access?: {
      roles?: Role[];
    };
  }
}

// The `JWT` interface can be found in the `next-auth/jwt` submodule
import "next-auth/jwt";

declare module "next-auth/jwt" {
  /** Returned by the `jwt` callback and `auth`, when using JWT sessions */
  interface JWT {
    /** OpenID ID Token */
    user?: User;
    tokens?: Tokens;
  }
}
