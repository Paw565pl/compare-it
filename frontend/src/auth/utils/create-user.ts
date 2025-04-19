import { User } from "@/auth/types/user";
import { Auth0Profile } from "next-auth/providers/auth0";

export const createUser = (auth0Profile: Auth0Profile): User => ({
  id: auth0Profile.sub,
  username: auth0Profile.preferred_username,
  email: auth0Profile.email,
  picture: auth0Profile.picture,
  roles: auth0Profile.realm_access?.roles,
});
