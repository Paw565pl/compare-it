import { auth } from "@/auth/config/auth-config";
import { prefetchCommentPage } from "@/comments/hooks/server/prefetch-comment-page";
import { getQueryClient } from "@/core/libs/tanstack-query";
import { getIsObjectIdValid } from "@/core/utils/get-is-object-id-valid";
import { ProductPageTop } from "@/products/components/index";
import { prefetchProduct } from "@/products/hooks/server/prefetch-product";
import { dehydrate, HydrationBoundary } from "@tanstack/react-query";
import { notFound } from "next/navigation";

export interface ProductPageProps {
  readonly params: Promise<{ id: string }>;
}

export const ProductPage = async ({ params }: ProductPageProps) => {
  const productId = (await params).id;
  const isProductIdValid = getIsObjectIdValid(productId);

  if (!isProductIdValid) return notFound();

  const session = await auth();
  const queryClient = getQueryClient();

  prefetchCommentPage(queryClient, productId, session?.tokens?.accessToken);
  await prefetchProduct(queryClient, productId);

  return (
    <HydrationBoundary state={dehydrate(queryClient)}>
      <ProductPageTop productId={productId} />
    </HydrationBoundary>
  );
};
