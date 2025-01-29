import { Role } from "@/auth/types/role";
import { Session } from "next-auth";

export const hasRequiredRole = (session: Session | null, role: Role) => {
  if (!session?.user?.roles) return false;

  const userRolesSet = new Set(
    session.user.roles.map((role) => role.toLowerCase()),
  );
  return userRolesSet.has(role.toLowerCase());
};
