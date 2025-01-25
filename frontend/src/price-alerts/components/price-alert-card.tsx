import { DeleteConfirmationAlertDialog } from "@/core/components/delete-confirmation-alert-dialog";
import {
  Card,
  CardContent,
  CardDescription,
  CardFooter,
  CardHeader,
  CardTitle,
} from "@/core/components/ui/card";
import { PriceAlertFormDialog } from "@/price-alerts/components/price-alert-form-dialog";
import { MockAlertData } from "@/price-alerts/components/price-alerts-grid";
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
        <PriceAlertFormDialog dialogTriggerLabel="Edytuj" />
      </CardFooter>
    </Card>
  );
};
