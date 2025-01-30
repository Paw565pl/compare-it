"use client";
import { ProductPage } from "@/products/pages/product-page";
import { useParams } from "next/navigation";

const ProductPageDefault = () => {
  const { id } = useParams();

  return <ProductPage id={id as string} />;
};

export default ProductPageDefault;
