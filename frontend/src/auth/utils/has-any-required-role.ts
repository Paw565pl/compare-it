import { Role } from "@/auth/types/role";
import { Session } from "next-auth";

export const hasAnyRequiredRole = (session: Session | null, roles: Role[]) => {
  if (!session?.user?.roles) return false;

  const userRolesSet = new Set(
    session.user.roles.map((role) => role.toLowerCase()),
  );
  return roles.some((role) => userRolesSet.has(role.toLowerCase()));
};
