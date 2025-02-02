"use client";

import { H1 } from "@/core/components/ui/h1";
import { PriceAlertCard } from "@/price-alerts/components";
import { useFetchPriceAlertsPage } from "@/price-alerts/hooks/client/use-fetch-price-alerts-page";
import { useSession } from "next-auth/react";
import InfiniteScroll from "react-infinite-scroll-component";

export const PriceAlertsGrid = () => {
  const { data: session } = useSession();

  const accessToken = session?.tokens?.accessToken as string;
  const userId = session?.user?.id as string;

  const {
    data: priceAlertsPage,
    hasNextPage,
    fetchNextPage,
    isError,
  } = useFetchPriceAlertsPage(accessToken, userId, { active: true });

  if (isError) return <span>Coś poszło nie tak!</span>;

  const isEmpty = priceAlertsPage?.pages.at(0)?.page.totalElements === 0;
  const fetchedPriceAlertsCount = priceAlertsPage?.pages.reduce(
    (acc, page) => acc + page.content.length,
    0,
  );

  return (
    <>
      <H1>Twoje alerty cenowe</H1>

      {isEmpty && (
        <div className="text-center sm:text-left">
          Nie masz jeszcze żadnych alertów cenowych.
        </div>
      )}

      <InfiniteScroll
        dataLength={fetchedPriceAlertsCount || 0}
        hasMore={hasNextPage}
        next={fetchNextPage}
        loader={null}
      >
        <section className="flex flex-wrap justify-center gap-2 sm:justify-start">
          {priceAlertsPage?.pages.map(({ content }) =>
            content?.map((priceAlert) => (
              <PriceAlertCard key={priceAlert.id} priceAlert={priceAlert} />
            )),
          )}
        </section>
      </InfiniteScroll>
    </>
  );
};
