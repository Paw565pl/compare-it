import { SingleOffer } from "@/products/components/index";
import { useFetchProduct } from "@/products/hooks/client/use-fetch-product";
import { ProductPageProps } from "@/products/pages/product-page";

const ProductPageOffers = ({ id }: ProductPageProps) => {
  const { data: productData, isLoading, error } = useFetchProduct(id);

  if (isLoading) return <div className="text-secondary">Ładowanie...</div>;
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
