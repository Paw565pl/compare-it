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
import { PriceAlertDto } from "@/price-alerts/dtos/price-alert-dto";
import { PriceAlertEntity } from "@/price-alerts/entities/price-alert-entity";
import { useDeletePriceAlert } from "@/price-alerts/hooks/client/use-delete-price-alert";
import { useUpdatePriceAlert } from "@/price-alerts/hooks/client/use-update-price-alert";
import { PriceAlertFormValues } from "@/price-alerts/schemas/price-alert-schema";
import { ProductImage } from "@/products/components";
import { useFetchProduct } from "@/products/hooks/client/use-fetch-product";
import { formatCurrency } from "@/products/utils/format-currency";
import { Pen } from "lucide-react";
import { useSession } from "next-auth/react";
import Link from "next/link";
import { toast } from "sonner";

interface PriceAlertCardProps {
  priceAlert: PriceAlertEntity;
}

export const PriceAlertCard = ({ priceAlert }: PriceAlertCardProps) => {
  const { data: session } = useSession();
  const accessToken = session?.tokens?.accessToken as string;

  const { data: product } = useFetchProduct(priceAlert.productId);
  const { mutate: updatePriceAlert } = useUpdatePriceAlert(
    accessToken,
    priceAlert.id,
  );
  const { mutate: deletePriceAlert } = useDeletePriceAlert(
    accessToken,
    priceAlert.id,
  );

  const handleUpdatePriceAlert = (formValues: PriceAlertFormValues) => {
    const priceAlertDto: PriceAlertDto = {
      productId: priceAlert.productId,
      targetPrice: Number(formValues.targetPrice),
      isOutletAllowed: formValues.isOutletAllowed,
    };

    updatePriceAlert(priceAlertDto, {
      onSuccess: () => toast.success("Alert cenowy został zaktualizowany."),
      onError: () => toast.error("Coś poszło nie tak!"),
    });
  };

  const formattedDesiredPrice = formatCurrency(priceAlert.targetPrice, "PLN");

  return (
    <Card className="w-[22rem]">
      <CardHeader>
        <div className="flex justify-center">
          <ProductImage
            name={priceAlert.productName}
            imageUrl={product?.images.at(0) || null}
          />
        </div>

        <CardTitle className="text-2xl">
          <Link href={`/produkty/${priceAlert.productId}`}>
            {priceAlert.productName}
          </Link>
        </CardTitle>

        <CardDescription className="flex items-center justify-between">
          <span>{product?.category}</span>
          <span>EAN: {product?.ean}</span>
        </CardDescription>
      </CardHeader>

      <CardContent>
        <p>
          Oczekiwana cena:{" "}
          <span className="font-bold">{formattedDesiredPrice}</span>
        </p>
        <p>
          Czy outlet jest dozwolony:{" "}
          <span className="font-bold">
            {priceAlert.outletAllowed ? "TAK" : "NIE"}
          </span>
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
          handleSubmit={handleUpdatePriceAlert}
          defaultValues={{
            targetPrice: priceAlert.targetPrice,
            isOutletAllowed: priceAlert.outletAllowed,
          }}
        />
      </CardFooter>
    </Card>
  );
};
