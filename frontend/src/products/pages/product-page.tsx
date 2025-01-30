import {
  ProductPageComments,
  ProductPageOffers,
} from "@/products/components/index";

interface ProductPageProps {
  readonly id: string;
}

export const ProductPage = ({ id }: ProductPageProps) => {
  return (
    <>
      <ProductPageOffers />
      <ProductPageComments />
    </>
  );
};
