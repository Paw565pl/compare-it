"use client";

import { DeleteConfirmationAlertDialog } from "@/core/components";
import { Button } from "@/core/components/ui/button";
import { FavoriteProductDto } from "@/favorite-products/dto/favorite-product-dto";
import { useAddFavoriteProduct } from "@/favorite-products/hooks/client/use-add-favorite-product";
import { useDeleteFavoriteProduct } from "@/favorite-products/hooks/client/use-delete-favorite-product";
import { useFavoriteProductStatus } from "@/favorite-products/hooks/client/use-favorite-product-status";
import { PriceAlertFormDialog } from "@/price-alerts/components";
import { PriceAlertDto } from "@/price-alerts/dtos/price-alert-dto";
import { useCreatePriceAlert } from "@/price-alerts/hooks/client/use-create-price-alert";
import { useDeletePriceAlert } from "@/price-alerts/hooks/client/use-delete-price-alert";
import { useFetchActivePriceAlertForProduct } from "@/price-alerts/hooks/client/use-fetch-active-price-alert-for-product";
import { useUpdatePriceAlert } from "@/price-alerts/hooks/client/use-update-price-alert";
import { PriceAlertFormValues } from "@/price-alerts/schemas/price-alert-schema";
import { Heart, HeartOff, Notebook, NotebookPen, Trash2 } from "lucide-react";
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

  const { data: favoriteProductStatus } = useFavoriteProductStatus(
    accessToken,
    userId,
    productId,
  );
  const { data: activePriceAlert } = useFetchActivePriceAlertForProduct(
    accessToken,
    userId,
    productId,
  );

  const { mutate: addFavoriteProduct } = useAddFavoriteProduct(accessToken);
  const { mutate: deleteFavoriteProduct } =
    useDeleteFavoriteProduct(accessToken);
  const { mutate: createPriceAlert } = useCreatePriceAlert(accessToken);
  const { mutate: updatePriceAlert } = useUpdatePriceAlert(
    accessToken,
    activePriceAlert?.id as string,
  );
  const { mutate: deletePriceAlert } = useDeletePriceAlert(
    accessToken,
    activePriceAlert?.id as string,
  );

  const handleAddFavoriteProduct = () => {
    if (!session)
      return toast.info(
        "Musisz być zalogowany, aby dodać produkt do ulubionych.",
      );

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

  const handleUpdatePriceAlert = (formValues: PriceAlertFormValues) => {
    const priceAlertDto: PriceAlertDto = {
      productId,
      targetPrice: Number(formValues.targetPrice),
      isOutletAllowed: formValues.isOutletAllowed,
    };

    updatePriceAlert(priceAlertDto, {
      onSuccess: () => toast.success("Alert cenowy został zaktualizowany."),
      onError: () => toast.error("Coś poszło nie tak!"),
    });
  };

  const handleDeletePriceAlert = () => {
    deletePriceAlert(undefined, {
      onSuccess: () => toast.success("Alert cenowy został usunięty."),
      onError: () => toast.error("Coś poszło nie tak!"),
    });
  };

  return (
    <>
      {favoriteProductStatus?.isFavorite ? (
        <Button
          onClick={handleDeleteFavoriteProduct}
          variant="invisible"
          size={"noPadding"}
        >
          <span className="flex items-center gap-2">
            <HeartOff />
            USUŃ Z ULUBIONYCH
          </span>
        </Button>
      ) : (
        <Button
          onClick={handleAddFavoriteProduct}
          variant="invisible"
          size={"noPadding"}
        >
          <span className="flex items-center gap-2">
            <Heart />
            POLUB
          </span>
        </Button>
      )}

      {activePriceAlert ? (
        <>
          <PriceAlertFormDialog
            trigger={
              <Button variant="invisible" size="noPadding">
                <span className="flex items-center gap-2">
                  <NotebookPen />
                  EDYTUJ ALERT CENOWY
                </span>
              </Button>
            }
            dialogHeader="Edytuj alert"
            handleSubmit={handleUpdatePriceAlert}
            defaultValues={{
              targetPrice: activePriceAlert.targetPrice.toString(),
              isOutletAllowed: activePriceAlert.isOutletAllowed,
            }}
          />

          <DeleteConfirmationAlertDialog
            trigger={
              <Button variant="invisible" size="noPadding">
                <Trash2 />
                <span>USUŃ ALERT</span>
              </Button>
            }
            handleDelete={handleDeletePriceAlert}
          />
        </>
      ) : session ? (
        <PriceAlertFormDialog
          trigger={
            <Button variant="invisible" size="noPadding">
              <span className="flex items-center gap-2">
                <Notebook />
                DODAJ ALERT CENOWY
              </span>
            </Button>
          }
          dialogHeader="Dodaj alert"
          handleSubmit={handleCreatePriceAlert}
        />
      ) : (
        <Button
          variant="invisible"
          size="noPadding"
          onClick={() =>
            toast.info("Musisz być zalogowany, aby stworzyć alert cenowy.")
          }
        >
          <span className="flex items-center gap-2">
            <Notebook />
            DODAJ ALERT CENOWY
          </span>
        </Button>
      )}
    </>
  );
};
