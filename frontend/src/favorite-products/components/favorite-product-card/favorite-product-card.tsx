import { DeleteConfirmationAlertDialog } from "@/core/components";
import {
  Card,
  CardContent,
  CardDescription,
  CardFooter,
  CardHeader,
  CardTitle,
} from "@/core/components/ui/card";
import { ImageWithFallback } from "@/core/components/ui/image-with-fallback";
import { formatCurrency } from "@/core/utils/format-currency";
import { useDeleteFavoriteProduct } from "@/favorite-products/hooks/client/use-delete-favorite-product";
import { ProductListEntity } from "@/products/entities/product-list-entity";
import { useSession } from "next-auth/react";
import Link from "next/link";
import { toast } from "sonner";

interface FavoriteProductCardProps {
  readonly product: ProductListEntity;
}

export const FavoriteProductCard = ({ product }: FavoriteProductCardProps) => {
  const { data: session } = useSession();
  const accessToken = session?.tokens?.accessToken as string;

  const { mutate: deleteFavoriteProduct } =
    useDeleteFavoriteProduct(accessToken);

  const handleDeleteFavoriteProduct = () => {
    deleteFavoriteProduct(
      { productId: product.id },
      {
        onSuccess: () => toast.success("Usunięto z ulubionych!"),
        onError: () => toast.error("Coś poszło nie tak!"),
      },
    );
  };

  const formattedPrice =
    product.lowestCurrentPrice && product.lowestPriceCurrency
      ? formatCurrency(product.lowestCurrentPrice, product.lowestPriceCurrency)
      : "-";

  return (
    <Card className="w-88">
      <CardHeader>
        <div className="flex justify-center">
          <ImageWithFallback
            src={product.mainImageUrl}
            alt={product.name}
            fill
            sizes="(max-width: 359px) 100vw, 320px"
            containerClassName="w-70 xs:w-75 h-66.25 bg-white"
          />
        </div>

        <CardTitle className="text-2xl">
          <Link href={`/produkty/${product.id}`}>{product.name}</Link>
        </CardTitle>

        <CardDescription className="flex items-center justify-between">
          <span>{product.category}</span>
          <span>EAN: {product.ean}</span>
        </CardDescription>
      </CardHeader>

      <CardContent className="flex items-center justify-between">
        <div className="flex items-center gap-2">
          {product.isAvailable ? (
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
        </div>

        <span className="text-primary text-2xl font-bold">
          {formattedPrice}
        </span>
      </CardContent>

      <CardFooter>
        <DeleteConfirmationAlertDialog
          alertDialogTriggerLabel="Usuń z ulubionych"
          alertDialogTriggerClassName="w-full"
          handleDelete={handleDeleteFavoriteProduct}
        />
      </CardFooter>
    </Card>
  );
};
