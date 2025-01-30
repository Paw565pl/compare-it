import { ProductImage } from "@/products/components/index";
import { useFetchProduct } from "@/products/hooks/client/use-fetch-product";
import { ProductPageProps } from "@/products/pages/product-page";

const ProductPageTop = ({ id }: ProductPageProps) => {
  const { data: productData, isLoading, error } = useFetchProduct(id);

  if (isLoading) return <div className="text-secondary">Ładowanie...</div>;
  if (error) return <div className="text-red-600">Coś poszło nie tak!</div>;
  return (
    <div className="border-grey-100 flex flex-col bg-white p-6 text-secondary md:flex-row">
      <div className="mb-4 flex-shrink-0 self-center md:mb-0 md:mr-6">
        <ProductImage
          name={productData?.name}
          imageUrl={productData?.images[0]}
        />
      </div>

      <div className="flex flex-grow flex-col justify-between">
        <div>
          <h1 className="mb-2 text-3xl font-bold">{productData?.name}</h1>
          <p className="mb-4 text-sm text-gray-500">
            Kategoria: {productData?.category}
          </p>

          <p className="text-sm text-gray-600">
            Liczba ofert: {productData?.offers.length}
          </p>
        </div>

        <div className="mt-1"></div>
      </div>
    </div>
  );
};

export { ProductPageTop };
