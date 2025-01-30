"use client";
import { Button } from "@/core/components/ui/button";
import { FavoriteProductDto } from "@/favorite-products/dto/favorite-product-dto";
import { useAddFavoriteProduct } from "@/favorite-products/hooks/client/use-add-favorite-product";
import { useDeleteFavoriteProduct } from "@/favorite-products/hooks/client/use-delete-favorite-product";
import { PriceAlertFormDialog } from "@/price-alerts/components/index";
import { PriceAlertDto } from "@/price-alerts/dtos/price-alert-dto";
import { useCreatePriceAlert } from "@/price-alerts/hooks/client/use-create-price-alert";
import { PriceAlertFormValues } from "@/price-alerts/schemas/price-alert-schema";
import {
  ProductPageComments,
  ProductPageImage,
  ProductPageImages,
  ProductPageOffers,
} from "@/products/components/index";
import { useFetchProduct } from "@/products/hooks/client/use-fetch-product";
import { ProductPageProps } from "@/products/pages/product-page";
import { Heart, HeartOff, Notebook } from "lucide-react";
import { useSession } from "next-auth/react";
import { useState } from "react";
import { toast } from "sonner";

const ProductPageTop = ({ id }: ProductPageProps) => {
  const { data: productData, isLoading, error } = useFetchProduct(id);
  const [category, setCategory] = useState("oferty");
  const { data: session } = useSession();

  const accessToken = session?.tokens?.accessToken as string;

  const { mutate: createPriceAlert } = useCreatePriceAlert(accessToken);
  const { mutate: addFavoriteProduct } = useAddFavoriteProduct(accessToken);
  const { mutate: deleteFavoriteProduct } =
    useDeleteFavoriteProduct(accessToken);

  if (isLoading) return <div className="text-secondary">Ładowanie...</div>;
  if (error) return <div className="text-red-600">Coś poszło nie tak!</div>;

  const handleCreatePriceAlert = (formValues: PriceAlertFormValues) => {
    const priceAlertDto: PriceAlertDto = {
      productId: id,
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
      productId: id,
    };

    addFavoriteProduct(favoriteProductDto, {
      onSuccess: () => toast.success("Polubiono produkt."),
      onError: () => toast.error("Ten produkt jest już polubiony!"),
    });
  };

  const handleDeleteFavoriteProduct = () => {
    const favoriteProductDto: FavoriteProductDto = {
      productId: id,
    };

    deleteFavoriteProduct(favoriteProductDto, {
      onSuccess: () => toast.success("Usunięto produkt z polubionych."),
      onError: () => toast.error("Ten produkt nie jest polubiony!"),
    });
  };

  return (
    <div className="flex flex-col">
      <div className="border-grey-100 flex flex-col bg-white p-6 text-secondary md:flex-row">
        <div className="mb-4 flex-shrink-0 self-center md:mb-0 md:mr-6">
          <ProductPageImage
            name={productData?.name}
            imageUrl={productData?.images[0]}
          />
        </div>

        <div className="flex flex-grow flex-col justify-between">
          <div>
            <h1 className="mb-2 text-3xl font-bold">{productData?.name}</h1>
            <p className="text-md mb-4 text-gray-500">
              Kod EAN: {productData?.ean}
            </p>
            <p className="text-md mb-4 text-gray-500">
              Kategoria: {productData?.category}
            </p>

            <p className="text-sm text-gray-600">
              Liczba ofert: {productData?.offers.length}
            </p>
            <div className="mt-4 flex w-min flex-col gap-4">
              <PriceAlertFormDialog
                dialogTrigger={
                  <Button className="cursor-pointer bg-secondary shadow-none hover:bg-hover">
                    <Notebook />
                    DODAJ ALERT CENOWY
                  </Button>
                }
                dialogHeader={"Dodaj alert"}
                handleSubmit={handleCreatePriceAlert}
              />
              <Button
                onClick={() => handleAddFavoriteProduct()}
                className="ml-0 cursor-pointer bg-secondary shadow-none hover:bg-hover"
              >
                <Heart />
                POLUB
              </Button>
              <Button
                onClick={() => handleDeleteFavoriteProduct()}
                className="ml-0 cursor-pointer bg-secondary shadow-none hover:bg-hover"
              >
                <HeartOff />
                USUŃ Z ULUBIONYCH
              </Button>
            </div>
          </div>
        </div>
      </div>

      <div className="mt-4 flex w-full bg-white">
        {["oferty", "obrazy", "opinie"].map((cat) => (
          <Button
            key={cat}
            onClick={() => setCategory(cat)}
            className={`w-full border-b-2 border-secondary font-semibold shadow-none transition-colors duration-200 ${
              category === cat
                ? "bg-secondary text-white hover:bg-secondary"
                : "bg-background text-gray-600 hover:bg-hover hover:text-white"
            }`}
          >
            {cat.toUpperCase()}
          </Button>
        ))}
      </div>

      {category === "oferty" && <ProductPageOffers id={id as string} />}
      {category === "obrazy" && (
        <ProductPageImages
          name={productData?.name}
          images={productData?.images}
        />
      )}
      {category === "opinie" && <ProductPageComments id={id as string} />}
    </div>
  );
};

export { ProductPageTop };
