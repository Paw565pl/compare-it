import { Button } from "@/core/components/ui/button";
import { H1 } from "@/core/components/ui/h1";
import Link from "next/link";

export const ErrorPage = () => {
  return (
    <>
      <H1>Ups! Wystąpił nieoczekiwany błąd</H1>
      <Link href="/produkty">
        <Button>Wróć do strony głównej</Button>
      </Link>
    </>
  );
};
