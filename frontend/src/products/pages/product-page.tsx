import {
  ProductPageComments,
  ProductPageOffers,
  ProductPageTop,
} from "@/products/components/index";

interface ProductPageProps {
  readonly id: string;
}

export const ProductPage = ({ id }: ProductPageProps) => {
  return (
    <>
      <ProductPageTop />
      <ProductPageOffers />
      <ProductPageComments />
    </>
  );
};
