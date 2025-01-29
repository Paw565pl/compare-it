import { H1 } from "@/core/components/ui/h1";
import { FavoriteProductCard } from "@/favorite-products/components";
import { useFetchFavoriteProductsPage } from "@/favorite-products/hooks/client/use-fetch-favorite-products-page";
import { useSession } from "next-auth/react";
import InfiniteScroll from "react-infinite-scroll-component";

export const FavoriteProductsGrid = () => {
  const { data: session } = useSession();

  const accessToken = session?.tokens?.accessToken as string;
  const userId = session?.user?.id as string;

  const {
    data: favoriteProductsPages,
    hasNextPage,
    fetchNextPage,
    isError,
  } = useFetchFavoriteProductsPage(accessToken, userId);

  if (isError) return <span>Coś poszło nie tak!</span>;

  const isEmpty = favoriteProductsPages?.pages.at(0)?.page.totalElements === 0;
  const fetchedFavoriteProductsCount = favoriteProductsPages?.pages.reduce(
    (acc, page) => acc + page.content.length,
    0,
  );

  return (
    <>
      <H1>Twoje ulubione produkty</H1>

      {isEmpty && <span>Nie masz jeszcze żadnych ulubionych produktów.</span>}

      <InfiniteScroll
        dataLength={fetchedFavoriteProductsCount || 0}
        hasMore={hasNextPage}
        next={fetchNextPage}
        loader={null}
      >
        <section className="mt-4 grid grid-cols-[repeat(auto-fill,minmax(350px,1fr))] justify-center justify-items-center gap-4 sm:justify-start sm:justify-items-start">
          {favoriteProductsPages?.pages.map(({ content }) =>
            content?.map((favoriteProduct) => (
              <FavoriteProductCard
                key={favoriteProduct.id}
                product={favoriteProduct}
              />
            )),
          )}
        </section>
      </InfiniteScroll>
    </>
  );
};
