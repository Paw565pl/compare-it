import { Button } from "@/core/components/ui/button";
import {
  Card,
  CardContent,
  CardDescription,
  CardFooter,
  CardHeader,
  CardTitle,
} from "@/core/components/ui/card";
import { formatCurrency } from "@/core/utils/format-currency";
import { formatDate } from "@/core/utils/format-date";
import { PriceAlertEntity } from "@/price-alerts/entities/price-alert-entity";
import { useDeletePriceAlert } from "@/price-alerts/hooks/client/use-delete-price-alert";
import { ProductImage } from "@/products/components";
import { useFetchProduct } from "@/products/hooks/client/use-fetch-product";
import { Clock, Trash2 } from "lucide-react";
import { useSession } from "next-auth/react";
import Link from "next/link";
import { toast } from "sonner";

interface PriceAlertNotificationCardProps {
  priceAlert: PriceAlertEntity;
}

export const PriceAlertNotificationCard = ({
  priceAlert,
}: PriceAlertNotificationCardProps) => {
  const { data: session } = useSession();
  const accessToken = session?.tokens?.accessToken as string;

  const { data: product } = useFetchProduct(priceAlert.productId);

  const { mutate: deletePriceAlert } = useDeletePriceAlert(
    accessToken,
    priceAlert.id,
  );

  const handleDeletePriceAlert = () => {
    deletePriceAlert(undefined, {
      onSuccess: () => toast.success("Alert cenowy został usunięty."),
      onError: () => toast.error("Coś poszło nie tak!"),
    });
  };

  const formattedPrice = formatCurrency(priceAlert.currentLowestPrice, "PLN");
  const formattedNotificationDate = formatDate(priceAlert.lastNotificationSent);

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

      <CardContent className="space-y-1">
        <div className="flex items-center justify-between">
          <div className="flex items-center gap-2 text-sm">
            <Clock className="h-5 w-5" />
            <span>{formattedNotificationDate}</span>
          </div>

          <div className="text-primary text-2xl font-bold">
            {formattedPrice}
          </div>
        </div>

        {/* <div className="flex items-center justify-between">
          <div className="flex items-center gap-2">
            <ShoppingCart className="h-5 w-5" />
            <span className="font-medium">{priceAlert.notification.shop}</span>
          </div>

          <div className="flex items-center gap-2">
            <BadgeCheck className="h-5 w-5" />
            <span className="capitalize">
              {priceAlert.notification.condition}
            </span>
          </div>
        </div> */}
      </CardContent>

      <CardFooter className="flex items-center justify-between">
        {/* <div className="flex items-center gap-2">
          {priceAlert.isAvailable ? (
            <>
              <span className="h-2 w-2 rounded-full bg-green-500" />
              <span className="text-sm text-green-500">Dostępny</span>
            </>
          ) : (
            <>
              <span className="h-2 w-2 rounded-full bg-red-500" />
              <span className="text-sm text-red-500">Niedostępny</span>
            </>
          )}
        </div> */}

        <Button variant="destructive" onClick={handleDeletePriceAlert}>
          <Trash2 /> Usuń z historii
        </Button>
      </CardFooter>
    </Card>
  );
};
