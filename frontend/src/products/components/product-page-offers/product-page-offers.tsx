import { SingleOffer } from "@/products/components";
import { useFetchProduct } from "@/products/hooks/client/use-fetch-product";

interface ProductPageOffersProps {
  readonly productId: string;
}

export const ProductPageOffers = ({ productId }: ProductPageOffersProps) => {
  const { data: productData, isLoading, error } = useFetchProduct(productId);

  if (isLoading) return <div className="text-primary">Ładowanie...</div>;
  if (error) return <div className="text-red-600">Coś poszło nie tak!</div>;

  return (
    <div className="mt-4">
      {productData?.offers.map((offer, offerIndex) => (
        <SingleOffer offer={offer} key={offerIndex} />
      ))}
    </div>
  );
};
