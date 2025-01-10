import { Role } from "@/auth/types/role";

export interface User {
  readonly id: string;
  readonly username?: string | null;
  readonly email?: string | null;
  readonly picture?: string | null;
  readonly roles?: Role[];
}
