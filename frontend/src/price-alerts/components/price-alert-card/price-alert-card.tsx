import { DeleteConfirmationAlertDialog } from "@/core/components";
import { Button } from "@/core/components/ui/button";
import {
  Card,
  CardContent,
  CardDescription,
  CardFooter,
  CardHeader,
  CardTitle,
} from "@/core/components/ui/card";
import { PriceAlertFormDialog } from "@/price-alerts/components";
import { MockAlertData } from "@/price-alerts/components/price-alerts-grid/price-alerts-grid";
import { ProductImage } from "@/products/components";
import { Pen } from "lucide-react";
import Link from "next/link";

interface PriceAlertCardProps {
  alertData: MockAlertData;
}

export const PriceAlertCard = ({ alertData }: PriceAlertCardProps) => {
  return (
    <Card className="w-[22rem]">
      <CardHeader>
        <div className="flex justify-center">
          <ProductImage
            name={alertData.name}
            imageUrl={alertData.mainImageUrl}
          />
        </div>

        <CardTitle className="text-2xl">
          <Link href={`/produkty/${alertData.id}`}>{alertData.name}</Link>
        </CardTitle>

        <CardDescription className="flex items-center justify-between">
          <span>{alertData.category}</span>
          <span>EAN: {alertData.ean}</span>
        </CardDescription>
      </CardHeader>

      <CardContent>
        <p>Oczekiwany stan: {alertData.alert.desiredCondition}</p>
        <p>
          Oczekiwana cena: {alertData.alert.desiredPrice}{" "}
          {alertData.alert.desiredCurrency}
        </p>
      </CardContent>

      <CardFooter className="flex items-center justify-between">
        <DeleteConfirmationAlertDialog
          handleDelete={() => console.log("delete price alert")}
        />
        <PriceAlertFormDialog
          dialogTrigger={
            <Button variant={"secondary"}>
              <Pen /> Edytuj
            </Button>
          }
          dialogHeader="Edytuj alert cenowy"
          handleSubmit={() => console.log("submit")}
        />
      </CardFooter>
    </Card>
  );
};
