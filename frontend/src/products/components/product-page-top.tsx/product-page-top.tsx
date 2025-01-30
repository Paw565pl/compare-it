"use client";
import { Button } from "@/core/components/ui/button";
import { ProductImage, ProductPageOffers } from "@/products/components/index";
import { useFetchProduct } from "@/products/hooks/client/use-fetch-product";
import { ProductPageProps } from "@/products/pages/product-page";
import { useState } from "react";

const ProductPageTop = ({ id }: ProductPageProps) => {
  const { data: productData, isLoading, error } = useFetchProduct(id);
  const [category, setCategory] = useState("oferty");

  if (isLoading) return <div className="text-secondary">Ładowanie...</div>;
  if (error) return <div className="text-red-600">Coś poszło nie tak!</div>;
  return (
    <div className="flex flex-col">
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
      <div className="mt-4 flex w-full bg-white">
        <Button
          onClick={() => setCategory("oferty")}
          className="w-full bg-white font-semibold text-black shadow-none hover:bg-hover hover:text-white"
        >
          OFERTY
        </Button>
        <Button
          onClick={() => setCategory("obrazy")}
          className="w-full bg-white font-semibold text-black shadow-none hover:bg-hover hover:text-white"
        >
          OBRAZY PRODUKTU
        </Button>
        <Button
          onClick={() => setCategory("opinie")}
          className="w-full bg-white font-semibold text-black shadow-none hover:bg-hover hover:text-white"
        >
          OPINIE
        </Button>
      </div>

      {category === "oferty" && <ProductPageOffers id={id as string} />}
    </div>
  );
};

export { ProductPageTop };
