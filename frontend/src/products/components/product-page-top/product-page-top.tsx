"use client";

import { CommentsSection } from "@/comments/components";
import { Button } from "@/core/components/ui/button";
import { H1 } from "@/core/components/ui/h1";
import { ImageWithFallback } from "@/core/components/ui/image-with-fallback";
import { cn } from "@/core/utils/cn";
import {
  ProductActionsButtons,
  ProductPageImages,
  ProductPageOffers,
} from "@/products/components/index";
import { useFetchProduct } from "@/products/hooks/client/use-fetch-product";
import { HttpStatusCode } from "axios";
import { notFound } from "next/navigation";
import { useState } from "react";

interface ProductPageTopProps {
  readonly productId: string;
}

const sections = ["oferty", "zdjęcia", "opinie"] as const;
type Section = (typeof sections)[number];

export const ProductPageTop = ({ productId }: ProductPageTopProps) => {
  const { data: productData, isLoading, error } = useFetchProduct(productId);
  const [activeSection, setActiveSection] = useState<Section>("oferty");

  if (isLoading) return <div className="text-primary">Ładowanie...</div>;
  if (
    error?.status === HttpStatusCode.NotFound ||
    error?.status === HttpStatusCode.BadRequest
  )
    return notFound();
  if (error || !productData)
    return <div className="text-red-600">Coś poszło nie tak!</div>;

  return (
    <div className="flex flex-col">
      <div className="border-grey-100 text-primary flex flex-col bg-white p-6 md:flex-row">
        <div className="mb-4 max-w-full self-center md:mr-6 md:mb-0">
          <ImageWithFallback
            src={productData.images.at(0) || ""}
            alt={productData.name}
            fill
            sizes="(max-width: 359px) 100vw, 320px"
            containerClassName="w-90 h-79.5 bg-white"
          />
        </div>

        <div className="flex h-full w-full flex-col justify-between">
          <div className="flex flex-col justify-between">
            <div className="mb-4">
              <H1 className="text-3xl font-bold">{productData.name}</H1>
              <p className="text-md mb-1 text-gray-500">
                Kod EAN: {productData.ean}
              </p>
              <p className="text-md mb-1 text-gray-500">
                Kategoria: {productData.category}
              </p>

              <p className="text-sm text-gray-600">
                Liczba ofert: {productData.offers.length}
              </p>
            </div>
            <ProductActionsButtons productId={productId} />
          </div>
        </div>
      </div>

      <div className="mt-4 flex w-full bg-white">
        {sections.map((section) => (
          <Button
            key={section}
            onClick={() => setActiveSection(section)}
            className={cn(
              "border-primary w-full border-b-2 font-semibold shadow-none transition-colors duration-200",
              activeSection === section
                ? "bg-primary hover:bg-primary text-white"
                : "bg-background hover:bg-hover text-gray-600 hover:text-white",
            )}
          >
            {section.toUpperCase()}
          </Button>
        ))}
      </div>

      {activeSection === "oferty" && (
        <ProductPageOffers productId={productId} />
      )}
      {activeSection === "zdjęcia" && (
        <ProductPageImages
          name={productData.name}
          images={productData.images}
        />
      )}
      {activeSection === "opinie" && <CommentsSection productId={productId} />}
    </div>
  );
};
