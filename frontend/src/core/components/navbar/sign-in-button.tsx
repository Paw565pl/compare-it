import { signInAction } from "@/auth/actions/auth-actions";
import { Button } from "@/core/components/ui/button";

export const SignInButton = () => {
  return (
    <form action={signInAction}>
      <Button type="submit">Zaloguj się</Button>
    </form>
  );
};
