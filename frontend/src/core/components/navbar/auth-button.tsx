import { signInAction, signOutAction } from "@/auth/actions/auth-actions";
import { auth } from "@/auth/config/auth-config";
import { Button } from "@/core/components/ui/button";

export const AuthButton = async () => {
  const session = await auth();

  if (!session) {
    return (
      <form action={signInAction}>
        <Button type="submit">Zaloguj się</Button>
      </form>
    );
  }

  return (
    <form action={signOutAction}>
      <Button type="submit">Wyloguj się</Button>
    </form>
  );
};
