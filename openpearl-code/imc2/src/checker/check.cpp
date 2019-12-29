
#include <memory>

#include "checker/check.hpp"
#include "checker/checks/check_associations_provided.hpp"
#include "checker/checks/check_params_match.hpp"
#include "checker/checks/check_username_sysname_mappings.hpp"
#include "checker/checks/check_pin_layout.hpp"
#include "checker/checks/check_multiple_dcl.hpp"
#include "checker/checks/check_spc_has_dcl.hpp"

using check_associations_provided     = ::imc::checker::checks::check_associations_provided;
using check_multiple_dcl              = ::imc::checker::checks::check_multiple_dcl;
using check_spc_has_dcl               = ::imc::checker::checks::check_spc_has_dcl;
using check_params_match              = ::imc::checker::checks::check_params_match;
using check_pin_layout                = ::imc::checker::checks::check_pin_layout;
using check_username_sysname_mappings = ::imc::checker::checks::check_username_sysname_mappings;

namespace imc {
    namespace checker {

        check::~check() {
            // nothing
        }

        std::vector<std::unique_ptr<check>> all_checks(
                const Platform& pl,
                const std::vector<Module>& mods) noexcept
        {
            std::vector<std::unique_ptr<check>> checks;

            checks.push_back(std::make_unique<check_params_match>(pl, mods));
            checks.push_back(std::make_unique<check_associations_provided>(pl, mods));
            checks.push_back(std::make_unique<check_pin_layout>(pl, mods));
            checks.push_back(std::make_unique<check_multiple_dcl>(pl, mods));
            checks.push_back(std::make_unique<check_spc_has_dcl>(pl, mods));

            // obsolete - covered by check_params_match
            //checks.push_back(std::make_unique<check_username_sysname_mappings>(pl, mods));

            return checks;
        }
    }
}

