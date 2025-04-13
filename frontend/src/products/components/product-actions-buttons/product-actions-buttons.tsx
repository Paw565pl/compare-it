import { Button } from "@/core/components/ui/button";
import { FavoriteProductDto } from "@/favorite-products/dto/favorite-product-dto";
import { useAddFavoriteProduct } from "@/favorite-products/hooks/client/use-add-favorite-product";
import { useDeleteFavoriteProduct } from "@/favorite-products/hooks/client/use-delete-favorite-product";
import { useFavoriteProductStatus } from "@/favorite-products/hooks/client/use-favorite-product-status";
import { PriceAlertFormDialog } from "@/price-alerts/components";
import { PriceAlertDto } from "@/price-alerts/dtos/price-alert-dto";
import { useCreatePriceAlert } from "@/price-alerts/hooks/client/use-create-price-alert";
import { PriceAlertFormValues } from "@/price-alerts/schemas/price-alert-schema";
import { Heart, HeartOff, Notebook } from "lucide-react";
import { useSession } from "next-auth/react";
import { toast } from "sonner";

interface ProductActionsButtonsProps {
  readonly productId: string;
}

export const ProductActionsButtons = ({
  productId,
}: ProductActionsButtonsProps) => {
  const { data: session } = useSession();
  const accessToken = session?.tokens?.accessToken as string;
  const userId = session?.user?.id as string;

  const { mutate: createPriceAlert } = useCreatePriceAlert(accessToken);
  const { mutate: addFavoriteProduct } = useAddFavoriteProduct(accessToken);
  const { mutate: deleteFavoriteProduct } =
    useDeleteFavoriteProduct(accessToken);

  const { data: favoriteProductStatus } = useFavoriteProductStatus(
    accessToken,
    productId,
    userId,
  );

  if (!session) return null;

  const handleCreatePriceAlert = (formValues: PriceAlertFormValues) => {
    const priceAlertDto: PriceAlertDto = {
      productId,
      targetPrice: Number(formValues.targetPrice),
      isOutletAllowed: formValues.isOutletAllowed,
    };

    createPriceAlert(priceAlertDto, {
      onSuccess: () => toast.success("Alert cenowy został utworzony."),
      onError: () => toast.error("Coś poszło nie tak!"),
    });
  };

  const handleAddFavoriteProduct = () => {
    const favoriteProductDto: FavoriteProductDto = {
      productId,
    };

    addFavoriteProduct(favoriteProductDto, {
      onSuccess: () => toast.success("Polubiono produkt."),
      onError: () => toast.error("Ten produkt jest już polubiony!"),
    });
  };

  const handleDeleteFavoriteProduct = () => {
    const favoriteProductDto: FavoriteProductDto = {
      productId,
    };

    deleteFavoriteProduct(favoriteProductDto, {
      onSuccess: () => toast.success("Usunięto produkt z polubionych."),
      onError: () => toast.error("Ten produkt nie jest polubiony!"),
    });
  };

  return (
    <div className="mt-4 flex w-full justify-between">
      <PriceAlertFormDialog
        dialogTrigger={
          <Button variant="priceAlert">
            <Notebook />
            DODAJ ALERT CENOWY
          </Button>
        }
        dialogHeader={"Dodaj alert"}
        handleSubmit={handleCreatePriceAlert}
      />
      {favoriteProductStatus?.isFavorite ? (
        <Button onClick={handleDeleteFavoriteProduct} variant="priceAlert">
          <HeartOff />
          USUŃ Z ULUBIONYCH
        </Button>
      ) : (
        <Button onClick={handleAddFavoriteProduct} variant="priceAlert">
          <Heart />
          POLUB
        </Button>
      )}
    </div>
  );
};
