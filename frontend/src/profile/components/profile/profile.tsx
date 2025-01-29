"use client";

import { Button } from "@/core/components/ui/button";
import { FavoriteProductsGrid } from "@/favorite-products/components/favorite-products-grid/favorite-products-grid";
import {
  PriceAlertsGrid,
  PriceAlertsNotificationsGrid,
} from "@/price-alerts/components";
import { UserDetailsCard } from "@/profile/components";
import { AlarmClock, Heart, NotebookPen, User } from "lucide-react";
import { ReactNode, useState } from "react";

interface AsideButton {
  readonly title: string;
  readonly component: ReactNode;
  readonly icon: ReactNode;
}

const asideButtonClassName = "!w-7 !h-7 !sm:w-10 !sm:h-10" as const;

export const Profile = () => {
  const [activeComponent, setActiveComponent] = useState<ReactNode>(
    <UserDetailsCard />,
  );

  const asideButtons: AsideButton[] = [
    {
      title: "Twój profil",
      component: <UserDetailsCard />,
      icon: <User className={asideButtonClassName} />,
    },
    {
      title: "Ulubione produkty",
      component: <FavoriteProductsGrid />,
      icon: <Heart className={asideButtonClassName} />,
    },
    {
      title: "Zarządzanie alertami",
      component: <PriceAlertsGrid />,
      icon: <NotebookPen className={asideButtonClassName} />,
    },
    {
      title: "Historia alertów",
      component: <PriceAlertsNotificationsGrid />,
      icon: <AlarmClock className={asideButtonClassName} />,
    },
  ];

  return (
    <div className="flex flex-col gap-2 sm:flex-row sm:gap-8">
      <aside className="mb-6 flex gap-2 sm:mb-0 sm:w-1/12 sm:flex-col sm:gap-4 xl:w-1/6">
        {asideButtons.map(({ title, component, icon }, index) => (
          <Button
            key={index}
            onClick={() => setActiveComponent(component)}
            variant="outline"
            size="icon"
            title={title}
            className="w-full bg-white p-6 sm:p-8"
          >
            {icon}
          </Button>
        ))}
      </aside>

      <section className="w-full">{activeComponent}</section>
    </div>
  );
};
