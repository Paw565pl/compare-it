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
import { PriceAlertEntity } from "@/price-alerts/entities/price-alert-entity";
import { ProductImage } from "@/products/components";
import { useFetchProduct } from "@/products/hooks/client/use-fetch-product";
import { formatCurrency } from "@/products/utils/format-currency";
import { Pen } from "lucide-react";
import Link from "next/link";

interface PriceAlertCardProps {
  priceAlert: PriceAlertEntity;
}

export const PriceAlertCard = ({ priceAlert }: PriceAlertCardProps) => {
  const { data: product } = useFetchProduct(priceAlert.productId);

  const formattedDesiredPrice = formatCurrency(priceAlert.targetPrice, "PLN");

  return (
    <Card className="w-[22rem]">
      <CardHeader>
        <div className="flex justify-center">
          <ProductImage
            name={priceAlert.productName}
            imageUrl={product?.images.at(0) || ""}
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
          handleSubmit={() => console.log("submit")}
        />
      </CardFooter>
    </Card>
  );
};
