import { auth } from "@/auth/config/auth-config";
import { SignInButton } from "@/core/components/navbar/sign-in-button";
import { SignOutButton } from "@/core/components/navbar/sign-out-button";

export const AuthSection = async () => {
  const session = await auth();

  if (!session) return <SignInButton />;

  return <SignOutButton />;
};
