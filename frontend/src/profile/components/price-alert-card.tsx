import { Button } from "@/core/components/ui/button";
import {
  Card,
  CardContent,
  CardDescription,
  CardFooter,
  CardHeader,
  CardTitle,
} from "@/core/components/ui/card";
import { DeletePriceAlertDialog } from "@/profile/components/delete-price-alert-dialog";
import { MockAlertData } from "@/profile/components/price-alerts-grid";
import Link from "next/link";

interface PriceAlertCardProps {
  alertData: MockAlertData;
}

export const PriceAlertCard = ({ alertData }: PriceAlertCardProps) => {
  return (
    <Card className="w-[350px]">
      <CardHeader>
        {/* FIXME: add image host to config and use Image component */}
        <img src={alertData.mainImageUrl} alt={alertData.name} />
        <CardTitle className="text-2xl">
          <Link href={`/produkty/${alertData.id}`}>{alertData.name}</Link>
        </CardTitle>
        <CardDescription>{alertData.category}</CardDescription>
      </CardHeader>
      <CardContent>
        <p>Oczekiwany stan: {alertData.alert.desiredCondition}</p>
        <p>
          Oczekiwana cena: {alertData.alert.desiredPrice}{" "}
          {alertData.alert.desiredCurrency}
        </p>
      </CardContent>
      <CardFooter className="flex items-center justify-between">
        <DeletePriceAlertDialog />
        <Button>Edytuj</Button>
      </CardFooter>
    </Card>
  );
};
