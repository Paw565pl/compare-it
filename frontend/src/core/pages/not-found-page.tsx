import { Button } from "@/core/components/ui/button";
import { H1 } from "@/core/components/ui/h1";
import Link from "next/link";

export const NotFoundPage = () => {
  return (
    <>
      <H1>404 - Nie znaleziono strony</H1>
      <p>Przepraszamy, nie mogliśmy znaleźć strony, której szukasz.</p>
      <Link href="/produkty">
        <Button className="mt-2">Wróć do strony głównej</Button>
      </Link>
    </>
  );
};
