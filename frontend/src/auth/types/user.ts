import { Role } from "@/auth/types/role";

export interface User {
  readonly id: string;
  readonly username: string;
  readonly email: string;
  readonly picture: string;
  readonly roles?: Role[];
}
