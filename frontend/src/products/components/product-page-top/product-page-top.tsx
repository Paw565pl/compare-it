"use client";

import { CommentsSection } from "@/comments/components";
import { Button } from "@/core/components/ui/button";
import { H1 } from "@/core/components/ui/h1";
import {
  ProductActionsButtons,
  ProductPageImage,
  ProductPageImages,
  ProductPageOffers,
} from "@/products/components/index";
import { useFetchProduct } from "@/products/hooks/client/use-fetch-product";
import { useState } from "react";

interface ProductPageTopProps {
  readonly productId: string;
}

const sections = ["oferty", "zdjęcia", "opinie"] as const;
type Section = (typeof sections)[number];

const ProductPageTop = ({ productId }: ProductPageTopProps) => {
  const { data: productData, isLoading, error } = useFetchProduct(productId);
  const [category, setCategory] = useState<Section>("oferty");

  if (isLoading) return <div className="text-secondary">Ładowanie...</div>;
  if (error) return <div className="text-red-600">Coś poszło nie tak!</div>;

  return (
    <div className="flex flex-col">
      <div className="border-grey-100 text-secondary flex flex-col bg-white p-6 md:flex-row">
        <div className="mb-4 shrink-0 self-center md:mr-6 md:mb-0">
          <ProductPageImage
            name={productData?.name || ""}
            imageUrl={productData?.images[0] || ""}
          />
        </div>

        <div className="flex grow flex-col justify-between">
          <div>
            <H1 className="mb-2 text-3xl font-bold">{productData?.name}</H1>
            <p className="text-md mb-4 text-gray-500">
              Kod EAN: {productData?.ean}
            </p>
            <p className="text-md mb-4 text-gray-500">
              Kategoria: {productData?.category}
            </p>

            <p className="text-sm text-gray-600">
              Liczba ofert: {productData?.offers.length}
            </p>

            <ProductActionsButtons productId={productId} />
          </div>
        </div>
      </div>

      <div className="mt-4 flex w-full bg-white">
        {sections.map((cat) => (
          <Button
            key={cat}
            onClick={() => setCategory(cat)}
            className={`border-secondary w-full border-b-2 font-semibold shadow-none transition-colors duration-200 ${
              category === cat
                ? "bg-secondary hover:bg-secondary text-white"
                : "bg-background hover:bg-hover text-gray-600 hover:text-white"
            }`}
          >
            {cat.toUpperCase()}
          </Button>
        ))}
      </div>

      {category === "oferty" && <ProductPageOffers productId={productId} />}
      {category === "zdjęcia" && (
        <ProductPageImages
          name={productData?.name || ""}
          images={productData?.images || []}
        />
      )}
      {category === "opinie" && <CommentsSection productId={productId} />}
    </div>
  );
};

export { ProductPageTop };
