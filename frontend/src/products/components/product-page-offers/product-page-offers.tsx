import { SingleOffer } from "@/products/components/index";
import { useFetchProduct } from "@/products/hooks/client/use-fetch-product";

interface ProductPageOffersProps {
  readonly productId: string;
}

const ProductPageOffers = ({ productId }: ProductPageOffersProps) => {
  const { data: productData, isLoading, error } = useFetchProduct(productId);

  if (isLoading) return <div className="text-primary">Ładowanie...</div>;
  if (error) return <div className="text-red-600">Coś poszło nie tak!</div>;

  return (
    <div className="mt-4">
      {productData?.offers.map((offer, offerIndex) => (
        <div key={offerIndex}>
          <SingleOffer offer={offer} />
        </div>
      ))}
    </div>
  );
};

export { ProductPageOffers };
